/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.shared

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dev.thomas_kiljanczyk.lyriccast.modules.FakeAppModule
import org.junit.Before
import org.junit.Rule

open class BaseHiltTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    open fun setup() {
        FakeAppModule.initializeDataStore(InstrumentationRegistry.getInstrumentation().targetContext)
        hiltRule.inject()
    }
}