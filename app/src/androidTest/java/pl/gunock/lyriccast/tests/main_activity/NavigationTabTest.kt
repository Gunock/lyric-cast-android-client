/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 2:43 PM
 */

package pl.gunock.lyriccast.tests.main_activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.MainActivity

@RunWith(AndroidJUnit4::class)
class NavigationTabTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun navigationTabsAreWorking() {
        onView(withId(R.id.text_input_song_filter))
            .check(matches(isDisplayed()))

        // TODO: Improve to be text independent
        onView(allOf(isDescendantOfA(withId(R.id.tab_layout_song_section)), withText("Setlists")))
            .perform(ViewActions.click())

        onView(withId(R.id.text_input_setlist_filter))
            .check(matches(isDisplayed()))

        // TODO: Improve to be text independent
        onView(allOf(isDescendantOfA(withId(R.id.tab_layout_song_section)), withText("Songs")))
            .perform(ViewActions.click())

        onView(withId(R.id.text_input_song_filter))
            .check(matches(isDisplayed()))
    }
}