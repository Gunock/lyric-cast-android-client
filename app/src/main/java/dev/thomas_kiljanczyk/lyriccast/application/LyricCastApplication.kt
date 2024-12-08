/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.application

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastMessageHelper
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastSessionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltAndroidApp
class LyricCastApplication : Application() {

    @Inject
    lateinit var dataStore: DataStore<AppSettings>

    @SuppressLint("WrongConstant")
    override fun onCreate() {
        super.onCreate()

        // Initializes CastContext
        CastContext.getSharedInstance(applicationContext, Executors.newSingleThreadExecutor())
            .addOnCompleteListener { task ->
                val listener = CastSessionListener(
                    onStarted = {
                        CoroutineScope(Dispatchers.Default).launch {
                            val blankOnStart = dataStore.data.first().blankOnStart
                            CastMessageHelper.sendBlank(blankOnStart)
                        }
                    },
                    onEnded = { CastMessageHelper.onSessionEnded() }
                )

                CoroutineScope(Dispatchers.Main).launch {
                    task.result.sessionManager.addSessionManagerListener(listener)
                }
            }


        DynamicColors.applyToActivitiesIfAvailable(this)

        // TODO: Add color harmonization

        dataStore.data
            .onEach {
                var appTheme: Int? = it.appTheme
                appTheme = if (appTheme == 0) null else appTheme
                if (appTheme != null) {
                    AppCompatDelegate.setDefaultNightMode(appTheme)
                }
            }.launchIn(CoroutineScope(Dispatchers.Main))


        val isDebuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (isDebuggable) {
            setupStrictMode()
        }
    }

    private fun setupStrictMode() {
        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .permitCustomSlowCalls()
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