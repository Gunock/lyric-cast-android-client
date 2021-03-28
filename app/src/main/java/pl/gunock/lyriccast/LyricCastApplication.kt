/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 3:18 AM
 */

package pl.gunock.lyriccast

import android.app.Application
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.LyricCastRoomDatabase

class LyricCastApplication : Application() {
    private val database by lazy {
        LyricCastRoomDatabase.getDatabase(applicationContext)
    }

    val repository by lazy {
        LyricCastRepository(
            database.songDao(),
            database.lyricsSectionDao(),
            database.setlistDao(),
            database.categoryDao()
        )
    }
}