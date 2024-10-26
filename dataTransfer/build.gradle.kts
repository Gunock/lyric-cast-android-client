/*
 * Created by Tomasz Kiljanczyk on 03/01/2022, 23:12
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 03/01/2022, 23:12
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    defaultConfig {
        minSdk = 27
        compileSdk = 34

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )

            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    namespace = "pl.gunock.datatransfer"
}

dependencies {
    // Submodules
    implementation(project(":common"))
}