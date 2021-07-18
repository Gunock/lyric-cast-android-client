/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:19
 */

package pl.gunock.lyriccast.application

import android.app.Application
import com.google.android.gms.cast.framework.CastContext
import io.realm.Realm
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.SessionStartedListener

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
                CastMessageHelper.sendBlank(LyricCastSettings.blankedOnStart)
            })

        super.onCreate()
    }
}