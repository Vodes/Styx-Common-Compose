plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

group = "moe.styx"
version = (System.getenv("SNAPSHOT_COMMIT") ?: "").ifBlank { "0.1.13" }

kotlin {
    applyDefaultHierarchyTemplate()
    jvmToolchain(17)
    jvm { withSourcesJar() }
    androidTarget { publishLibraryVariants("release") }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)

                api(libs.styx.common)

                // Other stuff
                api(libs.multiplatform.imageloader)
                api(libs.multiplatform.islandtime)
                api(libs.multiplatform.lifecycle)
                api(libs.multiplatform.settings)
                api(libs.multiplatform.semver)
                api(libs.kamel.image)
                api(libs.sonner)

                // IO
                api(libs.kstore)
                api(libs.kstore.file)

                // Navigation (Voyager)
                api(libs.voyager.nav)
                api(libs.voyager.transitions)
                api(libs.voyager.screenmodel)
                api(libs.voyager.tabnav)
                api(libs.voyager.bottomsheetnav)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.android.lifecycle.ktx)
            }
        }
        val iosMain by getting
        val jvmMain by getting {
            dependencies {
                implementation(libs.jvm.oshi)
            }
        }
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
            url =
                if (version.toString().contains("-SNAPSHOT", true) || !System.getenv("SNAPSHOT_COMMIT").isNullOrBlank())
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