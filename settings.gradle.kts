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
        maven("https://repo.styx.moe/snapshots")
    }
}

rootProject.name = "styx-common-compose"
include(":styx-common-compose")

val localCommon = file("../Styx-Common")
if (localCommon.isDirectory) {
    includeBuild(localCommon) {
        dependencySubstitution {
            substitute(module("moe.styx:styx-common"))
                .using(project(":styx-common"))
        }
    }
}
