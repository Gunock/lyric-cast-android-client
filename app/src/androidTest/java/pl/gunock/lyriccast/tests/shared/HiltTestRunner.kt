/*
 * Created by Tomasz Kiljanczyk on 08/01/2023, 21:58
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 08/01/2023, 21:57
 */

package pl.gunock.lyriccast.tests.shared

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication
import io.realm.Realm

@Suppress("unused")
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        Realm.init(context!!)
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}