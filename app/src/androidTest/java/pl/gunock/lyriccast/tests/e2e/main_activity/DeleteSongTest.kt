/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.e2e.main_activity

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import io.realm.RealmList
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.ui.main.MainActivity
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
class DeleteSongTest {

    private companion object {
        const val songTitle = "FilterSongsTest 1"
        val song1 =
            SongDocument("$songTitle 1", RealmList(), RealmList())
        val song2 = SongDocument("$songTitle 2", RealmList(), RealmList())
        val song3 = SongDocument("FilterSongsTest 2", RealmList(), RealmList())
    }

    @Before
    fun setUp() {
        getInstrumentation().runOnMainSync {
            val databaseViewModel = DatabaseViewModel.Factory(
                getInstrumentation().context.resources
            ).create()

            databaseViewModel.clearDatabase()

            databaseViewModel.upsertSong(song1)
            databaseViewModel.upsertSong(song2)
            databaseViewModel.upsertSong(song3)
        }

        ActivityScenario.launch(MainActivity::class.java)
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
        sleep(200)

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
        sleep(200)

        onView(withId(R.id.rcv_songs))
            .check(matches(not(hasDescendant(withText(song1.title)))))
            .check(matches(not(hasDescendant(withText(song2.title)))))
            .check(matches(hasDescendant(withText(song3.title))))
    }

}