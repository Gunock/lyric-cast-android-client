/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.ui.main_activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
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
import pl.gunock.lyriccast.shared.BaseHiltTest
import pl.gunock.lyriccast.shared.CustomEspresso.touchscreenClick
import pl.gunock.lyriccast.shared.CustomEspresso.touchscreenLongClick
import pl.gunock.lyriccast.shared.retryWithTimeout
import pl.gunock.lyriccast.ui.main.MainActivity
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