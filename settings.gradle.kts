pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://repo.styx.moe/releases")
    }
}

rootProject.name = "styx-common-compose"
include(":styx-common-compose")
