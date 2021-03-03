/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 10:56 PM
 */

package pl.gunock.lyriccast.tests.song_editor

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.MainActivity
import pl.gunock.lyriccast.adapters.SongItemsAdapter

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AddSongTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addSimpleSong() {
        val songTitle = "Test song"
        val songText = "Test test test"

        onView(withId(R.id.fab_add))
            .perform(click())
        onView(withId(R.id.fab_view_add_song))
            .check(matches(isDisplayed()))
        onView(withId(R.id.fab_view_add_setlist))
            .check(matches(isDisplayed()))

        onView(withId(R.id.fab_add_song))
            .perform(click())

        onView(withId(R.id.text_input_song_title))
            .perform(typeText(songTitle))
        closeSoftKeyboard()

        onView(withId(R.id.text_input_section_lyrics))
            .perform(typeText(songText))

        onView(withId(R.id.button_save_song))
            .perform(click())

        onView(withId(R.id.recycler_view_songs))
            .perform(
                RecyclerViewActions.scrollTo<SongItemsAdapter.SongViewHolder>(
                    hasDescendant(withText(songTitle))
                )
            )

        onView(withId(R.id.recycler_view_songs))
            .check(matches(hasDescendant(withText(songTitle))))
    }
}