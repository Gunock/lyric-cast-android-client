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
        compileSdk = 35
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    namespace = "pl.gunock.lyriccast.common"
}

dependencies {
    // Library dependencies
    implementation(libs.apache.commonsLang)
    implementation(libs.zip4j)
    implementation(libs.android.material)

    // Dependencies for local unit tests
    testImplementation(libs.junit)
    testImplementation(libs.google.truth)
}