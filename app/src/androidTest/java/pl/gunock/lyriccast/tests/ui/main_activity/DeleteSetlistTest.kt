/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.ui.main_activity

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.AllOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.RepositoryFactory
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.ui.main.MainActivity
import java.lang.Thread.sleep


class DeleteSetlistTest {

    private companion object {
        val setlist1 = Setlist("1", "DeleteSetlistTest 1", listOf())
        val setlist2 = Setlist("2", "DeleteSetlistTest 2", listOf())
        val setlist3 = Setlist("3", "DeleteSetlistTest 3", listOf())
    }

    @Before
    fun setUp() {
        val setlistsRepository = RepositoryFactory.createSetlistsRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )

        runBlocking {
            setlistsRepository.upsertSetlist(setlist1)
            setlistsRepository.upsertSetlist(setlist2)
            setlistsRepository.upsertSetlist(setlist3)
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