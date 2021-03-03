/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:55 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:26 PM
 */

package pl.gunock.lyriccast.tests.main_activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.MainActivity
import pl.gunock.lyriccast.utils.addSong

@RunWith(AndroidJUnit4::class)
class FilterSongsTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun songsAreFilteredByTitle() {
        val songTitle1 = "songsAreFilteredByTitle 1"
        val songTitle2 = "songsAreFilteredByTitle 2"
        val songTitle3 = "songsAreFilteredByTitle 3"

        addSong(songTitle1)
        addSong(songTitle2)
        addSong(songTitle3)

        onView(withId(R.id.rcv_songs))
            .check(matches(hasDescendant(withText(songTitle1))))
            .check(matches(hasDescendant(withText(songTitle2))))
            .check(matches(hasDescendant(withText(songTitle3))))

        onView(withId(R.id.tin_song_filter))
            .perform(replaceText(songTitle2.last().toString()))

        onView(withId(R.id.rcv_songs))
            .check(matches(hasDescendant(withText(songTitle2))))
            .check(matches(not(hasDescendant(withText(songTitle1)))))
            .check(matches(not(hasDescendant(withText(songTitle3)))))
    }

    @Test
    fun songsAreFilteredByCategory() {
        // TODO: Implement when song categories improved
    }

    @Test
    fun filterResetsAfterSongAdd() {
        // TODO: Add category when song categories improved

        onView(withId(R.id.tin_song_filter))
            .perform(replaceText("Test"))

        addSong("filterResetsAfterSongAdd 1")

        onView(withId(R.id.tin_song_filter))
            .check(matches(withText("")))
    }

}