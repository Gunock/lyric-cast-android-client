/*
 * Created by Tomasz Kiljanczyk on 12/12/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 11/12/2021, 23:09
 */

package pl.gunock.lyriccast.shared.extensions

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.application.Settings
import pl.gunock.lyriccast.application.settingsDataStore

fun Context.getSettings(): Settings {
    return runBlocking { settingsDataStore.data.first() }
}