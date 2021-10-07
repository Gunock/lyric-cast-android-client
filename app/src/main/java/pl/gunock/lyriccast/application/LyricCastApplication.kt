/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 18:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 18:43
 */

package pl.gunock.lyriccast.application

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.HiltAndroidApp
import pl.gunock.lyriccast.datamodel.RepositoryFactory
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.SessionStartedListener

@Suppress("unused")
@HiltAndroidApp
class LyricCastApplication : Application() {
    override fun onCreate() {
        // Initializes MongoDB Realm
        RepositoryFactory.initialize(applicationContext, RepositoryFactory.RepositoryProvider.MONGO)

        // Initializes CastContext
        CastContext.getSharedInstance(applicationContext)
            .sessionManager
            .addSessionManagerListener(SessionStartedListener {
                CastMessageHelper.sendBlank(LyricCastSettings.blankedOnStart)
            })

        MobileAds.initialize(applicationContext) {}
        CastMessageHelper.initialize(resources)
        LyricCastSettings.initialize(applicationContext)

        super.onCreate()
    }
}