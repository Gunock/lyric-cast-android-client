/*
 * Created by Tomasz Kiljanczyk on 16/05/2021, 17:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 16/05/2021, 17:06
 */

package pl.gunock.lyriccast.tests.integration.main_activity

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.RealmList
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.SetlistDocument
import pl.gunock.lyriccast.fragments.SetlistsFragment
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class FilterSetlistsTest {

    private companion object {
        const val setlistName = "FilterSetlistsTest 1"
        val setlist1 = SetlistDocument("$setlistName 1", RealmList())
        val setlist2 = SetlistDocument("$setlistName 2", RealmList())
        val setlist3 = SetlistDocument("FilterSetlistsTest 2", RealmList())
    }

    @Before
    fun setUp() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val databaseViewModel = DatabaseViewModel.Factory(
                InstrumentationRegistry.getInstrumentation().context.resources
            ).create()

            databaseViewModel.clearDatabase()

            databaseViewModel.upsertSetlist(setlist1)
            databaseViewModel.upsertSetlist(setlist2)
            databaseViewModel.upsertSetlist(setlist3)
        }

        launchFragmentInContainer<SetlistsFragment>(
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

        onView(withId(R.id.ed_setlist_filter)).perform(replaceText(setlistName))

        sleep(200)

        onView(withId(R.id.rcv_setlists))
            .check(matches(hasDescendant(withText(setlist1.name))))
            .check(matches(hasDescendant(withText(setlist2.name))))
            .check(matches(not(hasDescendant(withText(setlist3.name)))))
    }


}