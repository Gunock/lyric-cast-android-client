/*
 * Created by Tomasz Kiljanczyk on 03/01/2022, 23:17
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 03/01/2022, 23:13
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.mongoDbRealm)
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

    namespace = "pl.gunock.lyriccast.datamodel"
}

dependencies {
    // Submodules
    implementation(project(":dataTransfer"))

    // Library dependencies
    implementation(libs.kotlinx.coroutines)

    implementation(libs.mongoDbRealm.libraryBase)
}