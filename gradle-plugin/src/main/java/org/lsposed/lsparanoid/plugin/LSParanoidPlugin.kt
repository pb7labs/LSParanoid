/*
 * Copyright 2020 Michael Rozumyanskiy
 * Copyright 2023 LSPosed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lsposed.lsparanoid.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts.Scope
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.security.SecureRandom

class LSParanoidPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("lsparanoid", LSParanoidExtension::class.java)
        ANDROID_PLUGIN_IDS.forEach { androidPluginId ->
            project.pluginManager.withPlugin(androidPluginId) {
                project.configureAndroidComponents(extension)
            }
        }
        project.tasks.withType(JavaCompile::class.java) {
            it.options.compilerArgs.add("-XDstringConcat=inline")
        }
        project.tasks.withType(KotlinCompilationTask::class.java) {
            it.compilerOptions.freeCompilerArgs.add("-Xstring-concat=inline")
        }
    }

    private fun Project.configureAndroidComponents(extension: LSParanoidExtension) {
        // Configure only after an Android plugin ID is present. This avoids the
        // legacy AndroidBasePlugin type removed from AGP 9 and leaves AGP's
        // built-in Kotlin and KSP generated sources on their normal pipeline.
        val components = extensions.getByType(AndroidComponentsExtension::class.java)
        components.onVariants { variant ->
            if (!extension.variantFilter(variant)) return@onVariants
            val task = tasks.register(
                "lsparanoid${variant.name.replaceFirstChar { it.uppercase() }}", LSParanoidTask::class.java
            ) {
                it.bootClasspath.set(components.sdkComponents.bootClasspath)
                it.classpath = variant.compileClasspath
                it.seed.set(extension.seed ?: SecureRandom().nextInt())
                it.classFilter = extension.classFilter
                it.projectName.set("${rootProject.name}\$${path}")
            }
            variant.artifacts.forScope(if (extension.includeDependencies) Scope.ALL else Scope.PROJECT).use(task)
                .toTransform(
                    ScopedArtifact.CLASSES,
                    LSParanoidTask::jars,
                    LSParanoidTask::dirs,
                    LSParanoidTask::output,
                )
        }
        addDependencies()
    }

    private fun Project.addDependencies() {
        val version = Build.VERSION
        val configurationName = JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
        dependencies.add(configurationName, "org.lsposed.lsparanoid:core:$version")
    }

    private companion object {
        val ANDROID_PLUGIN_IDS = listOf(
            "com.android.application",
            "com.android.library",
            "com.android.dynamic-feature",
            "com.android.test",
        )
    }
}
