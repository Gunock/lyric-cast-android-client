/*
 * Created by Tomasz Kiljanczyk on 17/07/2021, 11:19
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 11:05
 */

package pl.gunock.lyriccast

import android.app.Application
import com.google.android.gms.cast.framework.CastContext
import io.realm.Realm
import pl.gunock.lyriccast.cast.SessionStartedListener
import pl.gunock.lyriccast.helpers.MessageHelper
import pl.gunock.lyriccast.models.LyricCastSettings

@Suppress("unused")
class LyricCastApplication : Application() {
    override fun onCreate() {
        // Initializes MongoDB Realm
        Realm.init(applicationContext)

        // Initializes CastContext
        CastContext.getSharedInstance(applicationContext)
        CastContext.getSharedInstance()!!
            .sessionManager
            .addSessionManagerListener(SessionStartedListener {
                MessageHelper.sendBlank(LyricCastSettings.blankedOnStart)
            })

        super.onCreate()
    }
}