/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.testing.HiltTestApplication
import java.util.concurrent.Executors

@Suppress("unused")
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: Context
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun callApplicationOnCreate(app: Application) {
        CastContext.getSharedInstance(app.applicationContext, Executors.newSingleThreadExecutor())

        super.callApplicationOnCreate(app)
    }
}