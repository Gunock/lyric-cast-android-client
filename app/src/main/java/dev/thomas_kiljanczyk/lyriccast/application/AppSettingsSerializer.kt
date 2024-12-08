/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.application

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings = AppSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppSettings {
        try {
            val settingsBuilder = AppSettings.parseFrom(input).toBuilder()
            setDefaultValues(settingsBuilder)

            return settingsBuilder.build()
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: AppSettings,
        output: OutputStream
    ) {
        t.writeTo(output)
    }

    private fun setDefaultValues(settingsBuilder: AppSettings.Builder) {
        if (settingsBuilder.appTheme == 0) {
            settingsBuilder.appTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        if (settingsBuilder.controlButtonsHeight == 0.0f) {
            settingsBuilder.controlButtonsHeight = 88.0f
        }
        if (settingsBuilder.backgroundColor.isBlank()) {
            settingsBuilder.backgroundColor = "Black"
        }
        if (settingsBuilder.fontColor.isBlank()) {
            settingsBuilder.fontColor = "White"
        }
        if (settingsBuilder.maxFontSize == 0) {
            settingsBuilder.maxFontSize = 90
        }
    }
}

val Context.settingsDataStore: DataStore<AppSettings> by dataStore(
    fileName = "settings.proto",
    serializer = AppSettingsSerializer
)