/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.tests.integration.main_activity

import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SmallTest
import dagger.hilt.android.testing.HiltAndroidTest
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.extensions.launchFragmentInHiltContainer
import dev.thomas_kiljanczyk.lyriccast.shared.BaseHiltTest
import dev.thomas_kiljanczyk.lyriccast.shared.retryWithTimeout
import dev.thomas_kiljanczyk.lyriccast.ui.main.setlists.SetlistsFragment
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class FilterSetlistsTest : BaseHiltTest() {

    private companion object {
        const val SETLIST_NAME = "FilterSetlistsTest 1"
        val setlist1 = Setlist("1", "$SETLIST_NAME 1", listOf())
        val setlist2 = Setlist("2", "$SETLIST_NAME 2", listOf())
        val setlist3 = Setlist("3", "FilterSetlistsTest 2", listOf())
    }

    @Inject
    lateinit var setlistsRepository: SetlistsRepository

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            setlistsRepository.upsertSetlist(setlist1)
            setlistsRepository.upsertSetlist(setlist2)
            setlistsRepository.upsertSetlist(setlist3)
        }

        launchFragmentInHiltContainer<SetlistsFragment>(
            bundleOf(),
            R.style.Theme_LyricCast_DarkActionBar
        )
    }

    @Test
    fun setlistsAreFilteredByName() {
        retryWithTimeout {
            onView(withId(R.id.rcv_setlists))
                .check(matches(hasDescendant(withText(setlist1.name))))
                .check(matches(hasDescendant(withText(setlist2.name))))
                .check(matches(hasDescendant(withText(setlist3.name))))
        }

        onView(withId(R.id.ed_setlist_name_filter))
            .perform(replaceText(SETLIST_NAME))

        retryWithTimeout {
            onView(withId(R.id.rcv_setlists))
                .check(matches(hasDescendant(withText(setlist1.name))))
                .check(matches(hasDescendant(withText(setlist2.name))))
                .check(matches(not(hasDescendant(withText(setlist3.name)))))
        }
    }


}