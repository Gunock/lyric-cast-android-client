/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 5:10 PM
 */

package pl.gunock.lyriccast

import android.app.Application
import com.google.android.gms.cast.framework.CastContext
import io.realm.Realm
import pl.gunock.lyriccast.cast.SessionStartedListener
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.LyricCastRoomDatabase
import pl.gunock.lyriccast.helpers.MessageHelper

class LyricCastApplication : Application() {
    private val mDatabase by lazy {
        LyricCastRoomDatabase.getDatabase(applicationContext)
    }

    val repository by lazy {
        LyricCastRepository(
            mDatabase.setlistDao()
        )
    }

    override fun onCreate() {
        // Initializes MongoDB Realm
        Realm.init(applicationContext)

        // Initializes CastContext
        CastContext.getSharedInstance(applicationContext)
        CastContext.getSharedInstance()!!
            .sessionManager
            .addSessionManagerListener(SessionStartedListener { MessageHelper.sendBlank(false) })

        super.onCreate()
    }
}