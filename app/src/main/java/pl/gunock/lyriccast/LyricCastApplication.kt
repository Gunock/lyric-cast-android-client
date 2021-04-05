/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:13 PM
 */

package pl.gunock.lyriccast

import android.app.Application
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.LyricCastRoomDatabase

class LyricCastApplication : Application() {
    private val mDatabase by lazy {
        LyricCastRoomDatabase.getDatabase(applicationContext)
    }

    val repository by lazy {
        LyricCastRepository(
            mDatabase.songDao(),
            mDatabase.lyricsSectionDao(),
            mDatabase.setlistDao(),
            mDatabase.categoryDao()
        )
    }
}