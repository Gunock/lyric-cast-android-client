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
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SmallTest
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.extensions.launchFragmentInHiltContainer
import pl.gunock.lyriccast.shared.BaseHiltTest
import pl.gunock.lyriccast.shared.retryWithTimeout
import pl.gunock.lyriccast.ui.main.setlists.SetlistsFragment
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class FilterSetlistsTest : BaseHiltTest() {

    private companion object {
        const val setlistName = "FilterSetlistsTest 1"
        val setlist1 = Setlist("1", "$setlistName 1", listOf())
        val setlist2 = Setlist("2", "$setlistName 2", listOf())
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
            .perform(replaceText(setlistName))

        retryWithTimeout {
            onView(withId(R.id.rcv_setlists))
                .check(matches(hasDescendant(withText(setlist1.name))))
                .check(matches(hasDescendant(withText(setlist2.name))))
                .check(matches(not(hasDescendant(withText(setlist3.name)))))
        }
    }


}