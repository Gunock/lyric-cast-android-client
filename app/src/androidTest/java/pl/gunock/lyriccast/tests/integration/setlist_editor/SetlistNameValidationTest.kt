/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 11:30
 */

package pl.gunock.lyriccast.tests.integration.setlist_editor

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.RealmList
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.SetlistDocument
import pl.gunock.lyriccast.ui.setlist_editor.SetlistEditorFragment

@RunWith(AndroidJUnit4::class)
class SetlistNameValidationTest {

    private companion object {
        const val longSetlistName = "SetlistNameValidationTest 2 very long name omg"
        val setlist = SetlistDocument("SetlistNameValidationTest 1", RealmList())
    }

    @Before
    fun setUp() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val databaseViewModel = DatabaseViewModel.Factory(
                InstrumentationRegistry.getInstrumentation().context.resources
            ).create()

            databaseViewModel.clearDatabase()

            databaseViewModel.upsertSetlist(setlist)
        }

        launchFragmentInContainer<SetlistEditorFragment>(
            bundleOf(),
            R.style.Theme_LyricCast_DarkActionBar
        )
    }

    @Test
    fun setlistNameAlreadyInUse() {
        onView(withId(R.id.ed_setlist_name))
            .perform(replaceText(setlist.name))

        onView(withId(R.id.tin_setlist_name))
            .check(matches(hasDescendant(withText("Setlist name already in use"))))
    }

    @Test
    fun setlistNameLengthIsLimited() {
        val maxNameLength = InstrumentationRegistry.getInstrumentation().targetContext
            .resources
            .getInteger(R.integer.ed_max_length_setlist_name)

        onView(withId(R.id.ed_setlist_name))
            .perform(replaceText(longSetlistName))

        val limitedSetlistName = longSetlistName.substring(0, maxNameLength)
        onView(withId(R.id.ed_setlist_name))
            .check(matches(withText(limitedSetlistName)))
    }

}