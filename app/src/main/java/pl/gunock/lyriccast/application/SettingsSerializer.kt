/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 19:57
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 12/11/2022, 19:02
 */

package pl.gunock.lyriccast.application

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer : Serializer<Settings> {
    override val defaultValue: Settings = Settings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Settings {
        try {
            val settingsBuilder = Settings.parseFrom(input).toBuilder()
            setDefaultValues(settingsBuilder)

            return settingsBuilder.build()
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: Settings,
        output: OutputStream
    ) {
        t.writeTo(output)
    }

    private fun setDefaultValues(settingsBuilder: Settings.Builder) {
        if (settingsBuilder.appTheme == 0) {
            settingsBuilder.appTheme = -1
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

val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings.proto",
    serializer = SettingsSerializer
)