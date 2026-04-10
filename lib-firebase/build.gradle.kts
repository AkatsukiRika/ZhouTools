plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget()

    val frameworkName = "lib-firebaseKit"
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = frameworkName
        }
    }
}

android {
    namespace = "com.tangping.lib.firebase"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    kotlin {
        jvmToolchain(11)
    }
}