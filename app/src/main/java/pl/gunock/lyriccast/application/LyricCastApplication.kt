/*
 * Created by Tomasz Kiljanczyk on 26/12/2022, 17:04
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 26/12/2022, 17:04
 */

package pl.gunock.lyriccast.application

import android.annotation.SuppressLint
import android.app.Application
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.BuildConfig
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.CastSessionListener
import pl.gunock.lyriccast.shared.extensions.getSettings
import java.util.concurrent.Executors

@HiltAndroidApp
class LyricCastApplication : Application() {
    @SuppressLint("WrongConstant")
    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            setupStrictMode()
        }

        initializeFromThread()

        // Initializes CastContext
        CastContext.getSharedInstance(applicationContext, Executors.newSingleThreadExecutor())
            .addOnCompleteListener { task ->
                val listener = CastSessionListener(
                    onStarted = { CastMessageHelper.sendBlank(getSettings().blankOnStart) },
                    onEnded = { CastMessageHelper.onSessionEnded() }
                )

                task.result.sessionManager.addSessionManagerListener(listener)
            }


        var appTheme = getSettings().appTheme
        appTheme = if (appTheme == 0) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else appTheme

        AppCompatDelegate.setDefaultNightMode(appTheme)

        super.onCreate()
    }

    private fun initializeFromThread() {
        CoroutineScope(Dispatchers.Default).launch {
            MobileAds.initialize(applicationContext)
        }
    }

    private fun setupStrictMode() {
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .penaltyDialog()
            .build()

        StrictMode.setThreadPolicy(threadPolicy)

        val vmPolicy = StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectFileUriExposure()
            .penaltyLog()
            .build()

        StrictMode.setVmPolicy(vmPolicy)
    }
}