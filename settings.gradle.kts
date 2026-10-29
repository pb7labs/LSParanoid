enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
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
        mavenLocal()
    }
}
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "LSParanoid"

include(
    ":core",
    ":processor",
    ":gradle-plugin"
)

includeBuild("samples")
