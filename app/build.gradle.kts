/*
 * Created by Tomasz Kiljanczyk on 03/01/2022, 23:17
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 03/01/2022, 23:13
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.navigationSafeArgs)
    alias(libs.plugins.hilt)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.google.googleServices)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    val major = 0
    val minor = 2
    val patch = 2

    defaultConfig {
        applicationId = "pl.gunock.lyriccast"
        minSdk = 27
        compileSdk = 35
        versionCode = major * 100000000 + minor * 10000 + patch
        versionName = "$major.$minor.$patch"

        testInstrumentationRunner = "pl.gunock.lyriccast.HiltTestRunner"
        resourceConfigurations += listOf("en", "pl")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        animationsDisabled = true

        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
    namespace = "pl.gunock.lyriccast"
}

dependencies {
    // Submodules
    implementation(project(":common"))
    implementation(project(":dataTransfer"))
    implementation(project(":dataModel"))

    // App dependencies
    implementation(libs.kotlinx.coroutines)

    // Architecture Components
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javaLite)

    // AndroidX
    implementation(libs.android.material)
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.preferenceKtx)
    implementation(libs.androidx.recyclerView)
    implementation(libs.androidx.recyclerViewSelection)
    implementation(libs.androidx.navigationFragmentKtx)
    implementation(libs.androidx.navigationUiKtx)

    // AdMob
    implementation(libs.google.ads)

    // Chromecast
    implementation(libs.google.castFramework)
    implementation(libs.androidx.mediaRouter)

    // Hilt
    implementation(libs.hilt)
    ksp(libs.hiltCompiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlyticsKtx)
    implementation(libs.firebase.analyticsKtx)

    // AndroidX Test - Instrumented testing
    androidTestImplementation(libs.androidx.test.coreKtx)
    androidTestImplementation(libs.androidx.test.extJunit)
    androidTestImplementation(libs.androidx.test.extJunitKtx)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.espressoContrib) {
        // TODO: Workaround for protobuf-lite test issues
        // Source: https://stackoverflow.com/questions/66154727/java-lang-nosuchmethoderror-no-static-method-registerdefaultinstance-with-fireb
        exclude(module = "protobuf-lite")
    }


    // AndroidX Test - Hilt testing
    androidTestImplementation(libs.hiltTesting)
    kspAndroidTest(libs.hiltCompiler)

    // LeakCanary
//    debugImplementation(libs.squareup.leakCanary)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

// A fix for protobuf ksp issues
// Source: https://github.com/google/ksp/issues/1590
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val capName = variant.name.replaceFirstChar { it.uppercase() }
            tasks.getByName<KotlinCompile>("ksp${capName}Kotlin") {
                setSource(tasks.getByName("generate${capName}Proto").outputs)
            }
        }
    }
}