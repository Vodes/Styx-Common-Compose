plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose)
    `maven-publish`
}

group = "moe.styx"
version = "0.0.1"

kotlin {
    applyDefaultHierarchyTemplate()
    jvm {
        jvmToolchain(11)
        withSourcesJar()
    }
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.styx.common)
            }
        }
        val androidMain by getting
        val iosMain by getting
        val jvmMain by getting
    }
}

android {
    namespace = "moe.styx.commonCompose"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

publishing {
    repositories {
        maven {
            name = "Styx"
            url = if (version.toString().contains("-SNAPSHOT", true))
                uri("https://repo.styx.moe/snapshots")
            else
                uri("https://repo.styx.moe/releases")
            credentials {
                username = System.getenv("STYX_REPO_TOKEN")
                password = System.getenv("STYX_REPO_SECRET")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}