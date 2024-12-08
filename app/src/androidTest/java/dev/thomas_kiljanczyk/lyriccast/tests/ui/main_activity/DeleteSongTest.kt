/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.tests.ui.main_activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidTest
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.shared.BaseHiltTest
import dev.thomas_kiljanczyk.lyriccast.shared.CustomEspresso.touchscreenClick
import dev.thomas_kiljanczyk.lyriccast.shared.CustomEspresso.touchscreenLongClick
import dev.thomas_kiljanczyk.lyriccast.shared.retryWithTimeout
import dev.thomas_kiljanczyk.lyriccast.ui.main.MainActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@LargeTest
class DeleteSongTest : BaseHiltTest() {

    private companion object {
        const val SONG_TITLE = "FilterSongsTest 1"
        val song1 = Song("1", "$SONG_TITLE 1", listOf(), listOf())
        val song2 = Song("2", "$SONG_TITLE 2", listOf(), listOf())
        val song3 = Song("3", "FilterSongsTest 2", listOf(), listOf())
    }

    @get:Rule(order = 1)
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Inject
    lateinit var songsRepository: SongsRepository

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            songsRepository.upsertSong(song1)
            songsRepository.upsertSong(song2)
            songsRepository.upsertSong(song3)
        }
    }

    @Test
    fun songIsDeleted() {
        retryWithTimeout {
            onView(withId(R.id.rcv_songs))
                .check(matches(hasDescendant(withText(song1.title))))
                .check(matches(hasDescendant(withText(song2.title))))
                .check(matches(hasDescendant(withText(song3.title))))
        }

        onView(
            allOf(withId(R.id.item_song), hasDescendant(withText(song2.title)))
        ).perform(touchscreenLongClick())
        onView(withId(R.id.action_menu_delete)).perform(click())

        retryWithTimeout {
            onView(withId(R.id.rcv_songs))
                .check(matches(hasDescendant(withText(song1.title))))
                .check(matches(not(hasDescendant(withText(song2.title)))))
                .check(matches(hasDescendant(withText(song3.title))))
        }
    }

    @Test
    fun multipleSongsAreDeleted() {
        retryWithTimeout {
            onView(withId(R.id.rcv_songs))
                .check(matches(hasDescendant(withText(song1.title))))
                .check(matches(hasDescendant(withText(song2.title))))
                .check(matches(hasDescendant(withText(song3.title))))
        }

        onView(
            allOf(withId(R.id.item_song), hasDescendant(withText(song1.title)))
        ).perform(touchscreenLongClick())
        onView(
            allOf(withId(R.id.item_song), hasDescendant(withText(song2.title)))
        ).perform(touchscreenClick())
        onView(withId(R.id.action_menu_delete)).perform(click())

        retryWithTimeout {
            onView(withId(R.id.rcv_songs))
                .check(matches(not(hasDescendant(withText(song1.title)))))
                .check(matches(not(hasDescendant(withText(song2.title)))))
                .check(matches(hasDescendant(withText(song3.title))))
        }
    }

}