/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 22:40
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 19:47
 */

package pl.gunock.lyriccast.tests.integration.main_activity

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
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
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.datamodel.models.mongo.SongDocument
import pl.gunock.lyriccast.ui.main.songs.SongsFragment
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
class FilterSongsTest {

    private companion object {
        val category = CategoryDocument("TEST CATEGORY", -65536)

        const val songTitle = "FilterSongsTest 1"
        val song1 =
            SongDocument("$songTitle 1", RealmList(), RealmList(), category = category)
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

            databaseViewModel.upsertCategory(category)
            databaseViewModel.upsertSong(song1)
            databaseViewModel.upsertSong(song2)
            databaseViewModel.upsertSong(song3)
        }

        launchFragmentInContainer<SongsFragment>(bundleOf(), R.style.Theme_LyricCast_DarkActionBar)
    }

    @Test
    fun songsAreFilteredByTitle() {
        onView(withId(R.id.rcv_songs))
            .check(matches(hasDescendant(withText(song1.title))))
            .check(matches(hasDescendant(withText(song2.title))))
            .check(matches(hasDescendant(withText(song3.title))))

        onView(withId(R.id.ed_song_title_filter)).perform(replaceText(songTitle))
        sleep(200)

        onView(withId(R.id.rcv_songs))
            .check(matches(hasDescendant(withText(song1.title))))
            .check(matches(hasDescendant(withText(song2.title))))
            .check(matches(not(hasDescendant(withText(song3.title)))))
    }

    @Test
    fun songsAreFilteredByCategory() {
        onView(withId(R.id.rcv_songs))
            .check(matches(hasDescendant(withText(song1.title))))
            .check(matches(hasDescendant(withText(song2.title))))
            .check(matches(hasDescendant(withText(song3.title))))

        onView(withId(R.id.spn_category)).perform(click())
        sleep(200)
        onView(
            allOf(
                withId(R.id.tv_spinner_color_name),
                withText(category.name)
            )
        ).perform(click())
        sleep(200)

        val allOfMatcher = allOf(
            hasDescendant(withText(song1.title)),
            hasDescendant(withText(category.name))
        )
        onView(withId(R.id.rcv_songs)).check(matches(hasDescendant(allOfMatcher)))
    }

}