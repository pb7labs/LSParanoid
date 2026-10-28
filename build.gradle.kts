import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.lsplugin.publish)
    alias(libs.plugins.kotlin) apply false
}


allprojects {
    group = "org.lsposed.lsparanoid"
    version = "0.6.0"

    plugins.withType(JavaPlugin::class.java) {
        extensions.configure(JavaPluginExtension::class.java) {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    // AGP 9 applies KotlinBasePlugin for built-in Kotlin but does not create a
    // KotlinJvmProjectExtension. Configure only the JVM modules in this build.
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure(KotlinJvmProjectExtension::class.java) {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}
