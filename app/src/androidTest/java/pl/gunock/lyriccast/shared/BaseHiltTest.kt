/*
 * Created by Tomasz Kiljanczyk on 5/12/24, 10:12 PM
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 5/12/24, 10:10 PM
 */

package pl.gunock.lyriccast.shared

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule
import pl.gunock.lyriccast.modules.FakeAppModule

open class BaseHiltTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    open fun setup() {
        FakeAppModule.initializeDataStore(InstrumentationRegistry.getInstrumentation().targetContext)
        hiltRule.inject()
    }
}