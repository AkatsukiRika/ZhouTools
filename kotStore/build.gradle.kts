plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget()
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "kotStore"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.datastore.core)
        }
    }
}

android {
    namespace = "com.tangping.kotstore"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    kotlin {
        jvmToolchain(8)
    }
}
