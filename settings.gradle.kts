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
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = "pk.eyJ1IjoiZHVjdHRzZTE3Mjc2MCIsImEiOiJjbWN6NGkyOTMwdTV5MmpzYTVibngyZW1oIn0.0uxPTusaG1JY5f3NuZ8O4Q"
            }
        }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = "pk.eyJ1IjoiZHVjdHRzZTE3Mjc2MCIsImEiOiJjbWN6NGkyOTMwdTV5MmpzYTVibngyZW1oIn0.0uxPTusaG1JY5f3NuZ8O4Q"

            }
        }
    }
}

rootProject.name = "PRM391_Project"
include(":app")
 