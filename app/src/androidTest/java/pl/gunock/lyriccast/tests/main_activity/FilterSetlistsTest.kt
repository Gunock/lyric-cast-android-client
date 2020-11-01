/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 2:43 PM
 */

package pl.gunock.lyriccast.tests.main_activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.MainActivity
import pl.gunock.lyriccast.utils.addSetlist

@RunWith(AndroidJUnit4::class)
class FilterSetlistsTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun setlistsAreFilteredByName() {
        val setlistName1 = "setlistsAreFilteredByName 1"
        val setlistName2 = "setlistsAreFilteredByName 2"
        val setlistName3 = "setlistsAreFilteredByName 3"

        addSetlist(setlistName1)
        addSetlist(setlistName2)
        addSetlist(setlistName3)

        // TODO: Improve to be text independent
        onView(allOf(isDescendantOfA(withId(R.id.tab_layout_song_section)), withText("Setlists")))
            .perform(click())

        onView(withId(R.id.recycler_view_setlists))
            .check(matches(hasDescendant(withText(setlistName1))))
            .check(matches(hasDescendant(withText(setlistName2))))
            .check(matches(hasDescendant(withText(setlistName3))))

        onView(withId(R.id.text_input_setlist_filter))
            .perform(replaceText(setlistName2.last().toString()))

        onView(withId(R.id.recycler_view_setlists))
            .check(matches(hasDescendant(withText(setlistName2))))
            .check(matches(not(hasDescendant(withText(setlistName1)))))
            .check(matches(not(hasDescendant(withText(setlistName3)))))
    }

    @Test
    fun setlistsAreFilteredByCategory() {
        // TODO: Implement when song categories improved
    }

    @Test
    fun filterResetsAfterSetlistAdd() {
        // TODO: Add category when song categories improved

        // TODO: Improve to be text independent
        onView(allOf(isDescendantOfA(withId(R.id.tab_layout_song_section)), withText("Setlists")))
            .perform(click())

        onView(withId(R.id.text_input_setlist_filter))
            .perform(replaceText("Test"))

        addSetlist("filterResetsAfterSetlistAdd 1")

        onView(withId(R.id.text_input_setlist_filter))
            .check(matches(withText("")))
    }

}