/*
 * Created by Tomasz Kiljanczyk on 12/12/2021, 12:57
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/12/2021, 12:55
 */

package pl.gunock.lyriccast.application

import android.annotation.SuppressLint
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.datamodel.RepositoryFactory
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.SessionStartedListener
import pl.gunock.lyriccast.shared.extensions.getSettings

// TODO: Resolve this problem
@SuppressLint("WrongConstant")
@HiltAndroidApp
class LyricCastApplication : Application() {
    override fun onCreate() {
        // Initializes MongoDB Realm
        RepositoryFactory.initialize(applicationContext, RepositoryFactory.RepositoryProvider.MONGO)

        // Initializes settings
        CoroutineScope(Dispatchers.IO).launch {
            applicationContext.settingsDataStore.data.first()
        }

        // Initializes CastContext
        CastContext.getSharedInstance(applicationContext)
            .sessionManager
            .addSessionManagerListener(SessionStartedListener {
                CastMessageHelper.sendBlank(applicationContext.getSettings().blankOnStart)
            })

        MobileAds.initialize(applicationContext) {}
        CastMessageHelper.initialize(resources)


        AppCompatDelegate.setDefaultNightMode(applicationContext.getSettings().appTheme)

        super.onCreate()
    }
}