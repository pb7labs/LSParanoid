import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.lsplugin.publish)
    alias(libs.plugins.kotlin) apply false
}


allprojects {
    group = "io.github.pb7labs"
    version = "0.6.1"

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

    // Configure Sonatype Central Portal OSSRH Staging API endpoint
    pluginManager.withPlugin("maven-publish") {
        extensions.configure(PublishingExtension::class.java) {
            repositories.configureEach {
                if (name == "ossrh" && this is MavenArtifactRepository) {
                    url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                }
            }
        }
    }
}

val promoteCentralPortal by tasks.registering {
    group = "publishing"
    description = "Triggers finalization/promotion for Central Portal staging upload for namespace io.github.pb7labs"
    doLast {
        val username = providers.gradleProperty("ossrhUsername").orNull ?: System.getenv("OSSRH_USERNAME")
        val password = providers.gradleProperty("ossrhPassword").orNull ?: System.getenv("OSSRH_PASSWORD")
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            throw GradleException("Central Portal user-token credentials (ossrhUsername & ossrhPassword) are required to promote the deployment.")
        }
        val targetUrl = "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/manual/upload/defaultRepository/io.github.pb7labs"
        val connection = java.net.URI.create(targetUrl).toURL().openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "POST"
        val auth = java.util.Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        connection.setRequestProperty("Authorization", "Basic $auth")
        connection.setRequestProperty("Content-Type", "application/xml")
        val responseCode = connection.responseCode
        if (responseCode in 200..299) {
            logger.lifecycle("Successfully triggered Central Portal staging repository promotion (HTTP $responseCode).")
        } else {
            val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            throw GradleException("Failed to promote Central Portal staging repository (HTTP $responseCode): $errorStream")
        }
    }
}
