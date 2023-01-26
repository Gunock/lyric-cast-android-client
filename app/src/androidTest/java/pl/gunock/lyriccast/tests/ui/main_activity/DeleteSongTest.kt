/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.ui.main_activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.ui.main.MainActivity
import java.lang.Thread.sleep
import javax.inject.Inject

@HiltAndroidTest
@LargeTest
class DeleteSongTest {

    private companion object {
        const val songTitle = "FilterSongsTest 1"
        val song1 = Song("1", "$songTitle 1", listOf(), listOf())
        val song2 = Song("2", "$songTitle 2", listOf(), listOf())
        val song3 = Song("3", "FilterSongsTest 2", listOf(), listOf())
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Inject
    lateinit var songsRepository: SongsRepository

    @Before
    fun setup() {
        hiltRule.inject()

        runBlocking {
            songsRepository.upsertSong(song1)
            songsRepository.upsertSong(song2)
            songsRepository.upsertSong(song3)
        }
        sleep(100)
    }

    @Test
    fun songIsDeleted() {
        onView(withId(R.id.rcv_songs))
            .check(matches(hasDescendant(withText(song1.title))))
            .check(matches(hasDescendant(withText(song2.title))))
            .check(matches(hasDescendant(withText(song3.title))))

        onView(
            allOf(withId(R.id.item_song), hasDescendant(withText(song2.title)))
        ).perform(longClick())
        onView(withId(R.id.action_menu_delete)).perform(click())
        sleep(100)

        onView(withId(R.id.rcv_songs))
            .check(matches(hasDescendant(withText(song1.title))))
            .check(matches(not(hasDescendant(withText(song2.title)))))
            .check(matches(hasDescendant(withText(song3.title))))
    }

    @Test
    fun multipleSongsAreDeleted() {
        onView(withId(R.id.rcv_songs))
            .check(matches(hasDescendant(withText(song1.title))))
            .check(matches(hasDescendant(withText(song2.title))))
            .check(matches(hasDescendant(withText(song3.title))))

        onView(
            allOf(withId(R.id.item_song), hasDescendant(withText(song1.title)))
        ).perform(longClick())
        onView(
            allOf(withId(R.id.item_song), hasDescendant(withText(song2.title)))
        ).perform(click())
        onView(withId(R.id.action_menu_delete)).perform(click())
        sleep(100)

        onView(withId(R.id.rcv_songs))
            .check(matches(not(hasDescendant(withText(song1.title)))))
            .check(matches(not(hasDescendant(withText(song2.title)))))
            .check(matches(hasDescendant(withText(song3.title))))
    }

}