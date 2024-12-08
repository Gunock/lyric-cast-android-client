/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.tests.integration.setlist_editor

import android.graphics.Color
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidTest
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.extensions.launchFragmentInHiltContainer
import dev.thomas_kiljanczyk.lyriccast.shared.BaseHiltTest
import dev.thomas_kiljanczyk.lyriccast.shared.retryWithTimeout
import dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.songs.SetlistEditorSongsFragment
import dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.songs.SetlistEditorSongsFragmentArgs
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@LargeTest
class FilterSetlistEditorSongsTest : BaseHiltTest() {

    private companion object {
        val category = Category("TEST CATEGORY", Color.RED)

        const val SONG_TITLE = "FilterSongsTest 1"
        val song1 = Song("1", "$SONG_TITLE 1", listOf(), listOf(), category = category)
        val song2 = Song("2", "$SONG_TITLE 2", listOf(), listOf())
        val song3 = Song("3", "FilterSongsTest 2", listOf(), listOf())
    }

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            categoriesRepository.upsertCategory(category)

            songsRepository.upsertSong(song1)
            songsRepository.upsertSong(song2)
            songsRepository.upsertSong(song3)
        }

        val bundle = SetlistEditorSongsFragmentArgs(
            setlistId = "1",
            setlistName = "",
            presentation = arrayOf(song2.id)
        ).toBundle()

        launchFragmentInHiltContainer<SetlistEditorSongsFragment>(
            bundle,
            R.style.Theme_LyricCast_DarkActionBar
        )
    }

    @Test
    fun songsAreFilteredByTitle() {
        retryWithTimeout {
            onView(withId(R.id.rcv_songs))
                .check(matches(hasDescendant(withText(song1.title))))
                .check(matches(hasDescendant(withText(song2.title))))
                .check(matches(hasDescendant(withText(song3.title))))
        }

        onView(withId(R.id.ed_song_title_filter))
            .perform(replaceText(SONG_TITLE))

        retryWithTimeout {
            onView(withId(R.id.rcv_songs))
                .check(matches(hasDescendant(withText(song1.title))))
                .check(matches(hasDescendant(withText(song2.title))))
                .check(matches(not(hasDescendant(withText(song3.title)))))
        }
    }

    @Test
    fun songsAreFilteredByCategory() {
        retryWithTimeout {
            onView(withId(R.id.rcv_songs))
                .check(matches(hasDescendant(withText(song1.title))))
                .check(matches(hasDescendant(withText(song2.title))))
                .check(matches(hasDescendant(withText(song3.title))))
        }

        onView(withId(R.id.dropdown_category)).perform(click())
        retryWithTimeout {
            onView(
                allOf(withId(R.id.text_color_name), withText(category.name))
            ).inRoot(RootMatchers.isPlatformPopup())
                .perform(click())
        }

        retryWithTimeout {
            val allOfMatcher = allOf(
                hasDescendant(withText(song1.title)),
                hasDescendant(withText(category.name))
            )
            onView(withId(R.id.rcv_songs)).check(matches(hasDescendant(allOfMatcher)))
        }
    }

    @Test
    fun songsAreFilteredBySelection() {
        retryWithTimeout {
            onView(withId(R.id.rcv_songs))
                .check(matches(hasDescendant(withText(song1.title))))
                .check(
                    matches(
                        allOf(hasDescendant(withText(song2.title)), hasDescendant(isChecked()))
                    )
                )
                .check(matches(hasDescendant(withText(song3.title))))
        }

        onView(withId(R.id.swt_selected_songs)).perform(click())

        retryWithTimeout {
            onView(withId(R.id.rcv_songs))
                .check(matches(not(hasDescendant(withText(song1.title)))))
                .check(matches(hasDescendant(withText(song2.title))))
                .check(matches(not(hasDescendant(withText(song3.title)))))
        }
    }

}