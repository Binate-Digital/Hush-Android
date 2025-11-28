pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {url = uri("https://maven.google.com")  }
        maven { url = uri("https://jitpack.io")  }
        maven { url = uri("https://maven.twilio.com/releases")  }

    }
}

rootProject.name = "Hush"
include(":app",":player")
 