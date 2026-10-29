enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val parentLocalProps = file("../local.properties")
val localProps = file("local.properties")
if (parentLocalProps.exists() && !localProps.exists()) {
    parentLocalProps.copyTo(localProps)
}

pluginManagement {
    includeBuild("..")
    val requestedAgpVersion = providers.gradleProperty("agpVersion").orElse("9.0.1")
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android.")) {
                useVersion(requestedAgpVersion.get())
            }
        }
    }
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "LSParanoid-Samples"

include(
    ":application",
    ":application-global-obfuscate",
    ":library",
    ":library-may-obfuscate",
    ":library-obfuscate"
)
