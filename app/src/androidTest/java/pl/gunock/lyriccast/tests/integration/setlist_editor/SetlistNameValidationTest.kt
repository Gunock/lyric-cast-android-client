/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.integration.setlist_editor

import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.launchFragmentInHiltContainer
import pl.gunock.lyriccast.ui.setlist_editor.setlist.SetlistEditorFragment
import java.lang.Thread.sleep
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class SetlistNameValidationTest {

    private companion object {
        const val longSetlistName = "SetlistNameValidationTest 2 very long name omg"
        val setlist = Setlist("1", "SetlistNameValidationTest 1", listOf())
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var setlistsRepository: SetlistsRepository

    @Before
    fun setup() {
        hiltRule.inject()

        runBlocking {
            setlistsRepository.upsertSetlist(setlist)
        }
        sleep(100)

        launchFragmentInHiltContainer<SetlistEditorFragment>(
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