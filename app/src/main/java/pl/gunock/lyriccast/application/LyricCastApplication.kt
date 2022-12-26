/*
 * Created by Tomasz Kiljanczyk on 26/12/2022, 17:04
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 26/12/2022, 17:04
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
        // TODO: Fix deprecation
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


        val appTheme = applicationContext.getSettings().appTheme
        AppCompatDelegate.setDefaultNightMode(if (appTheme == 0) -1 else appTheme)

        super.onCreate()
    }

    private fun initializeFromThread() {
        CoroutineScope(Dispatchers.Default).launch {
            // Initializes settings
            applicationContext.settingsDataStore.data.first()

            MobileAds.initialize(applicationContext) {}
        }
    }
}