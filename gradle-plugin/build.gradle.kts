import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.tasks.Jar

plugins {
    idea
    alias(libs.plugins.kotlin)
    `java-gradle-plugin`
    `maven-publish`
    signing
}

gradlePlugin {
    plugins {
        create("lsparanoid") {
            id = "io.github.pb7labs.lsparanoid"
            implementationClass = "org.lsposed.lsparanoid.plugin.LSParanoidPlugin"
        }
    }
}


dependencies {
    implementation(projects.core)
    implementation(projects.processor)
    implementation(libs.kotlin.api)
    implementation(libs.agp.api)
}

val generatedDir = File(projectDir, "generated")
val generatedJavaSourcesDir = File(generatedDir, "main/java")

val genTask = tasks.register("generateBuildClass") {
    inputs.property("version", version)
    outputs.dir(generatedDir)
    doLast {
        val buildClassFile =
            File(generatedJavaSourcesDir, "org/lsposed/lsparanoid/plugin/Build.java")
        buildClassFile.parentFile.mkdirs()
        buildClassFile.writeText(
            """
            package org.lsposed.lsparanoid.plugin;
            /**
             * The type Build.
             */
            public class Build {
               /**
                * The constant VERSION.
                */
               public static final String VERSION = "$version";
            }""".trimIndent()
        )
    }
}

sourceSets {
    main {
        java {
            srcDir(generatedJavaSourcesDir)
        }
    }
}

tasks.withType(KotlinCompile::class.java) {
    dependsOn(genTask)
}

tasks.withType(Jar::class.java) {
    dependsOn(genTask)
}

idea {
    module {
        generatedSourceDirs.add(generatedJavaSourcesDir)
    }
}

publish {
    githubRepo = "pb7labs/LSParanoid"
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("LSParanoid")
            description.set("String obfuscator for Android applications")
            url.set("https://github.com/pb7labs/LSParanoid")
            licenses {
                license {
                    name.set("Apache License 2.0")
                    url.set("https://github.com/pb7labs/LSParanoid/blob/master/LICENSE.txt")
                }
            }
            developers {
                developer {
                    name.set("Pulkit Bhanot")
                    url.set("https://github.com/pb7labs")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/pb7labs/LSParanoid.git")
                url.set("https://github.com/pb7labs/LSParanoid")
            }
        }
    }
}
