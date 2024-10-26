pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

        //روش جدید بازار
        maven { setUrl( "https://jitpack.io" )}
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        //روش جدید
        maven { setUrl( "https://jitpack.io" )}
    }
}

rootProject.name = "BazaarPayment"
include(":app")
 