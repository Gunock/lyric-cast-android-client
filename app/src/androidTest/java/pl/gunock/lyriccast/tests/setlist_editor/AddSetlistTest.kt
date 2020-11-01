/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 3:43 PM
 */

package pl.gunock.lyriccast.tests.setlist_editor

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.MainActivity
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.utils.addSong


@RunWith(AndroidJUnit4::class)
class AddSetlistTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addSimpleSetlist() {
        val songTitle1 = "addSimpleSetlist 1"
        val songTitle2 = "addSimpleSetlist 2"
        val songTitle3 = "addSimpleSetlist 3"
        val setlistName = "addSimpleSetlist 1"

        addSong(songTitle1)
        addSong(songTitle2)
        addSong(songTitle3)

        onView(withId(R.id.fab_add))
            .perform(click())
        onView(withId(R.id.fab_view_add_song))
            .check(matches(isDisplayed()))
        onView(withId(R.id.fab_view_add_setlist))
            .check(matches(isDisplayed()))

        onView(withId(R.id.fab_add_setlist))
            .perform(click())

        onView(withId(R.id.text_input_setlist_name))
            .perform(typeText(setlistName))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.button_pick_setlist_songs))
            .perform(click())

        onView(withId(R.id.recycler_view_songs))
            .perform(
                RecyclerViewActions.scrollTo<SongListAdapter.SongViewHolder>(
                    hasDescendant(withText(songTitle3))
                )
            )

        onView(allOf(withId(R.id.song_checkbox), hasSibling(withText(songTitle1))))
            .perform(click())
        onView(allOf(withId(R.id.song_checkbox), hasSibling(withText(songTitle2))))
            .perform(click())
        onView(allOf(withId(R.id.song_checkbox), hasSibling(withText(songTitle3))))
            .perform(click())

        // TODO: Move song picking to separate activity
        Espresso.pressBack()

        onView(withId(R.id.recycler_view_songs))
            .check(matches(hasDescendant(withText(songTitle1))))
            .check(matches(hasDescendant(withText(songTitle2))))
            .check(matches(hasDescendant(withText(songTitle3))))

        onView(withId(R.id.button_save_setlist))
            .perform(click())

        // TODO: Improve to be text independent
        onView(allOf(isDescendantOfA(withId(R.id.tab_layout_song_section)), withText("Setlists")))
            .perform(click())

        onView(withId(R.id.recycler_view_setlists))
            .check(matches(hasDescendant(withText(setlistName))))
    }
}