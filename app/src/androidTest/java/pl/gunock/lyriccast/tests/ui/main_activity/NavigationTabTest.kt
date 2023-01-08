/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 19:31
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 18:53
 */

package pl.gunock.lyriccast.tests.ui.main_activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.ui.main.MainActivity

class NavigationTabTest {
    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun navigationTabsAreWorking() {
        onView(withId(R.id.ed_song_title_filter))
            .check(matches(isDisplayed()))

        onView(allOf(isDescendantOfA(withId(R.id.tbl_main_fragments)), withText("Setlists")))
            .perform(click())

        onView(withId(R.id.ed_setlist_name_filter))
            .check(matches(isDisplayed()))

        onView(allOf(isDescendantOfA(withId(R.id.tbl_main_fragments)), withText("Songs")))
            .perform(click())

        onView(withId(R.id.ed_song_title_filter))
            .check(matches(isDisplayed()))
    }
}