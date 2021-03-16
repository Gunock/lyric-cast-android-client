/*
 * Created by Tomasz Kiljańczyk on 3/15/21 11:49 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 11:45 AM
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
import pl.gunock.lyriccast.adapters.SongItemsAdapter
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
        onView(withId(R.id.LinearLayout2))
            .check(matches(isDisplayed()))
        onView(withId(R.id.LinearLayout1))
            .check(matches(isDisplayed()))

        onView(withId(R.id.fab_add_setlist))
            .perform(click())

        onView(withId(R.id.tin_setlist_name))
            .perform(typeText(setlistName))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.btn_pick_setlist_songs))
            .perform(click())

        onView(withId(R.id.rcv_songs))
            .perform(
                RecyclerViewActions.scrollTo<SongItemsAdapter.ViewHolder>(
                    hasDescendant(withText(songTitle3))
                )
            )

        onView(allOf(withId(R.id.chk_item_song), hasSibling(withText(songTitle1))))
            .perform(click())
        onView(allOf(withId(R.id.chk_item_song), hasSibling(withText(songTitle2))))
            .perform(click())
        onView(allOf(withId(R.id.chk_item_song), hasSibling(withText(songTitle3))))
            .perform(click())

        // TODO: Move song picking to separate activity
        Espresso.pressBack()

        onView(withId(R.id.rcv_songs))
            .check(matches(hasDescendant(withText(songTitle1))))
            .check(matches(hasDescendant(withText(songTitle2))))
            .check(matches(hasDescendant(withText(songTitle3))))

        onView(withId(R.id.btn_save_setlist))
            .perform(click())

        // TODO: Improve to be text independent
        onView(allOf(isDescendantOfA(withId(R.id.tbl_song_section)), withText("Setlists")))
            .perform(click())

        onView(withId(R.id.rcv_setlists))
            .check(matches(hasDescendant(withText(setlistName))))
    }
}