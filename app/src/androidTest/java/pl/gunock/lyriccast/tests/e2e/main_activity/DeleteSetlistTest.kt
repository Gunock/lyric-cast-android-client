/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:19
 */

package pl.gunock.lyriccast.tests.e2e.main_activity

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import io.realm.RealmList
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.AllOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.SetlistDocument
import pl.gunock.lyriccast.ui.main.MainActivity
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
class DeleteSetlistTest {

    private companion object {
        val setlist1 = SetlistDocument("DeleteSetlistTest 1", RealmList())
        val setlist2 = SetlistDocument("DeleteSetlistTest 2", RealmList())
        val setlist3 = SetlistDocument("DeleteSetlistTest 3", RealmList())
    }

    @Before
    fun setUp() {
        getInstrumentation().runOnMainSync {
            val databaseViewModel = DatabaseViewModel.Factory(
                getInstrumentation().context.resources
            ).create()

            databaseViewModel.clearDatabase()

            databaseViewModel.upsertSetlist(setlist1)
            databaseViewModel.upsertSetlist(setlist2)
            databaseViewModel.upsertSetlist(setlist3)
        }

        ActivityScenario.launch(MainActivity::class.java)
        onView(AllOf.allOf(isDescendantOfA(withId(R.id.tbl_main_fragments)), withText("Setlists")))
            .perform(click())

    }

    @Test
    fun setlistIsDeleted() {
        onView(withId(R.id.rcv_setlists))
            .check(matches(hasDescendant(withText(setlist1.name))))
            .check(matches(hasDescendant(withText(setlist2.name))))
            .check(matches(hasDescendant(withText(setlist3.name))))

        onView(
            allOf(withId(R.id.item_setlist), hasDescendant(withText(setlist2.name)))
        ).perform(longClick())
        onView(withId(R.id.action_menu_delete)).perform(click())
        sleep(200)

        onView(withId(R.id.rcv_setlists))
            .check(matches(hasDescendant(withText(setlist1.name))))
            .check(matches(not(hasDescendant(withText(setlist2.name)))))
            .check(matches(hasDescendant(withText(setlist3.name))))
    }

    @Test
    fun multipleSetlistsAreDeleted() {
        onView(withId(R.id.rcv_setlists))
            .check(matches(hasDescendant(withText(setlist1.name))))
            .check(matches(hasDescendant(withText(setlist2.name))))
            .check(matches(hasDescendant(withText(setlist3.name))))

        onView(
            allOf(withId(R.id.item_setlist), hasDescendant(withText(setlist1.name)))
        ).perform(longClick())
        onView(
            allOf(withId(R.id.item_setlist), hasDescendant(withText(setlist2.name)))
        ).perform(click())
        onView(withId(R.id.action_menu_delete)).perform(click())
        sleep(200)

        onView(withId(R.id.rcv_setlists))
            .check(matches(not(hasDescendant(withText(setlist1.name)))))
            .check(matches(not(hasDescendant(withText(setlist2.name)))))
            .check(matches(hasDescendant(withText(setlist3.name))))
    }

}