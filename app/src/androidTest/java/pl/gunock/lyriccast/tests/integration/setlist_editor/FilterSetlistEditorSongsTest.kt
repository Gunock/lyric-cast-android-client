/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 18:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 18:43
 */

package pl.gunock.lyriccast.tests.integration.setlist_editor

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
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.extensions.launchFragmentInHiltContainer
import pl.gunock.lyriccast.shared.BaseHiltTest
import pl.gunock.lyriccast.shared.retryWithTimeout
import pl.gunock.lyriccast.ui.setlist_editor.songs.SetlistEditorSongsFragment
import pl.gunock.lyriccast.ui.setlist_editor.songs.SetlistEditorSongsFragmentArgs
import javax.inject.Inject

@HiltAndroidTest
@LargeTest
class FilterSetlistEditorSongsTest : BaseHiltTest() {

    private companion object {
        val category = Category("TEST CATEGORY", Color.RED)

        const val songTitle = "FilterSongsTest 1"
        val song1 = Song("1", "$songTitle 1", listOf(), listOf(), category = category)
        val song2 = Song("2", "$songTitle 2", listOf(), listOf())
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
            .perform(replaceText(songTitle))

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