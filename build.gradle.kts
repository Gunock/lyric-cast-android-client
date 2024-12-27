/*
 * Created by Tomasz Kiljanczyk on 21/12/2021, 00:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 21/12/2021, 00:25
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.androidx.navSafeArgs)
        classpath(libs.google.googleServices)
        classpath(libs.firebase.crashlyticsGradle)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}


plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.mongoDbRealm) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.google.googleServices) apply false
}
