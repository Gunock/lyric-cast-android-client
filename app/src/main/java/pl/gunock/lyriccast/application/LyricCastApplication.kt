/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 18:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 18:15
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
import pl.gunock.lyriccast.shared.cast.CastSessionListener
import pl.gunock.lyriccast.shared.extensions.getSettings

// TODO: Resolve this problem
@SuppressLint("WrongConstant")
@HiltAndroidApp
class LyricCastApplication : Application() {
    override fun onCreate() {
        initializeFromThread()

        // Initializes MongoDB Realm
        RepositoryFactory.initialize(applicationContext, RepositoryFactory.RepositoryProvider.MONGO)

        // Initializes CastContext
        CastContext.getSharedInstance(applicationContext)
            .sessionManager
            .addSessionManagerListener(
                CastSessionListener(
                    onStarted = {
                        CastMessageHelper.sendBlank(applicationContext.getSettings().blankOnStart)
                    },
                    onEnded = { CastMessageHelper.onSessionEnded() }
                )
            )


        AppCompatDelegate.setDefaultNightMode(applicationContext.getSettings().appTheme)

        super.onCreate()
    }

    private fun initializeFromThread() {
        CoroutineScope(Dispatchers.Default).launch {
            // Initializes settings
            applicationContext.settingsDataStore.data.first()

            MobileAds.initialize(applicationContext) {}
            CastMessageHelper.initialize(resources)
        }
    }
}