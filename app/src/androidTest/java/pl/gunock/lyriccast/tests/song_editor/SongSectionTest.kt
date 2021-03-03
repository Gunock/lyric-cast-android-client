/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:55 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:26 PM
 */

package pl.gunock.lyriccast.tests.song_editor

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.SongEditorActivity

@RunWith(AndroidJUnit4::class)
class SongSectionTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<SongEditorActivity> =
        ActivityScenarioRule(SongEditorActivity::class.java)

    @Test
    fun songSectionIsRenamed() {
        val sectionName = "Test"

        onView(withId(R.id.tin_section_name))
            .perform(replaceText(sectionName))
        closeSoftKeyboard()

        onView(allOf(isDescendantOfA(withId(R.id.tbl_song_section)), withText(sectionName)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun songSectionIsAdded() {
        val sectionName = "New section"

        onView(allOf(isDescendantOfA(withId(R.id.tbl_song_section)), withText("Add")))
            .perform(click())

        onView(allOf(isDescendantOfA(withId(R.id.tbl_song_section)), withText(sectionName)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun songSectionTextIsStored() {
        val sectionName = "New section"
        val songText = "Test test test"

        onView(allOf(isDescendantOfA(withId(R.id.tbl_song_section)), withText("Add")))
            .perform(click())

        onView(withId(R.id.tin_section_lyrics)).perform(typeText(songText))
        closeSoftKeyboard()

        onView(allOf(isDescendantOfA(withId(R.id.tbl_song_section)), withText("Chorus")))
            .perform(click())

        onView(withId(R.id.tin_section_lyrics)).check(matches(withText("")))

        onView(allOf(isDescendantOfA(withId(R.id.tbl_song_section)), withText(sectionName)))
            .perform(click())

        onView(withId(R.id.tin_section_lyrics)).check(matches(withText(songText)))
    }
}