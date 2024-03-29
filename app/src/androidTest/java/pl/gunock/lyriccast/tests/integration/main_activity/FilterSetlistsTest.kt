/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 19:31
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 18:53
 */

package pl.gunock.lyriccast.tests.integration.main_activity

import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.SmallTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.launchFragmentInHiltContainer
import pl.gunock.lyriccast.ui.main.setlists.SetlistsFragment
import java.lang.Thread.sleep
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class FilterSetlistsTest {

    private companion object {
        const val setlistName = "FilterSetlistsTest 1"
        val setlist1 = Setlist("1", "$setlistName 1", listOf())
        val setlist2 = Setlist("2", "$setlistName 2", listOf())
        val setlist3 = Setlist("3", "FilterSetlistsTest 2", listOf())
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var setlistsRepository: SetlistsRepository

    @Before
    fun setup() {
        hiltRule.inject()

        runBlocking {
            setlistsRepository.upsertSetlist(setlist1)
            setlistsRepository.upsertSetlist(setlist2)
            setlistsRepository.upsertSetlist(setlist3)
        }
        sleep(100)

        launchFragmentInHiltContainer<SetlistsFragment>(
            bundleOf(),
            R.style.Theme_LyricCast_DarkActionBar
        )
    }

    @Test
    fun setlistsAreFilteredByName() {
        onView(withId(R.id.rcv_setlists))
            .check(matches(hasDescendant(withText(setlist1.name))))
            .check(matches(hasDescendant(withText(setlist2.name))))
            .check(matches(hasDescendant(withText(setlist3.name))))

        onView(withId(R.id.ed_setlist_name_filter))
            .perform(replaceText(setlistName))

        // Test needs to accommodate for debounce
        sleep(700)

        onView(withId(R.id.rcv_setlists))
            .check(matches(hasDescendant(withText(setlist1.name))))
            .check(matches(hasDescendant(withText(setlist2.name))))
            .check(matches(not(hasDescendant(withText(setlist3.name)))))
    }


}