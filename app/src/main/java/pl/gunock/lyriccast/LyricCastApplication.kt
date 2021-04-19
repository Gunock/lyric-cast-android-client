/*
 * Created by Tomasz Kiljanczyk on 4/20/21 1:10 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 1:09 AM
 */

package pl.gunock.lyriccast

import android.app.Application
import com.google.android.gms.cast.framework.CastContext
import io.realm.Realm
import pl.gunock.lyriccast.cast.SessionStartedListener
import pl.gunock.lyriccast.helpers.MessageHelper

@Suppress("unused")
class LyricCastApplication : Application() {
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