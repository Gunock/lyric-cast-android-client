/*
 * Created by Tomasz Kiljanczyk on 08/01/2023, 23:50
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 08/01/2023, 23:04
 */

package pl.gunock.lyriccast.tests.shared

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