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
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.extensions.launchFragmentInHiltContainer
import pl.gunock.lyriccast.shared.BaseHiltTest
import pl.gunock.lyriccast.ui.setlist_editor.setlist.SetlistEditorFragment
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class SetlistNameValidationTest : BaseHiltTest() {

    private companion object {
        const val LONG_SETLIST_NAME = "SetlistNameValidationTest 2 very long name omg"
        val setlist = Setlist("1", "SetlistNameValidationTest 1", listOf())
    }

    @Inject
    lateinit var setlistsRepository: SetlistsRepository

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            setlistsRepository.upsertSetlist(setlist)
        }

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
            .perform(replaceText(LONG_SETLIST_NAME))

        val limitedSetlistName = LONG_SETLIST_NAME.substring(0, maxNameLength)
        onView(withId(R.id.ed_setlist_name))
            .check(matches(withText(limitedSetlistName)))
    }

}