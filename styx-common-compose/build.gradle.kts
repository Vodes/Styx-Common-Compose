import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

group = "moe.styx"
version = (System.getenv("SNAPSHOT_COMMIT") ?: "").ifBlank { "0.6.1" }

kotlin {
    applyDefaultHierarchyTemplate()
    jvmToolchain(21)
    jvm {
        withSourcesJar()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    android {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        namespace = "moe.styx.commonCompose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)

                api(libs.styx.common)
                api(libs.styx.mal)
                api(libs.anilist.kmp)

                // Other stuff
                api(libs.multiplatform.islandtime)
                api(libs.multiplatform.lifecycle)
                api(libs.multiplatform.settings)
                api(libs.multiplatform.semver)
                api(libs.kamel.image)
                api(libs.sonner)

                // IO
                api(libs.kstore)
                api(libs.kstore.file)

                // Navigation
                api(libs.vortex)

                // Dialogs
                api(libs.dialogs.core)
                api(libs.dialogs.list)
                api(libs.dialogs.number)
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        getByName("androidMain") {
            dependencies {
                implementation(libs.android.lifecycle.ktx)
            }
        }
        getByName("iosMain")
        getByName("jvmMain") {
            dependencies {
                implementation(libs.jvm.oshi)
            }
        }
    }
}

compose.resources {
    publicResClass = true
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
