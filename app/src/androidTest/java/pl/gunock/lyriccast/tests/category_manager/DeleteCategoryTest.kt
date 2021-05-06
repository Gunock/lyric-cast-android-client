/*
 * Created by Tomasz Kiljanczyk on 06/05/2021, 13:47
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/05/2021, 13:47
 */

package pl.gunock.lyriccast.tests.category_manager

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.CategoryManagerActivity
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
class DeleteCategoryTest {

    private companion object {
        val category1 = CategoryDocument("DELETE_CATEGORY_TEST 1", -65536)
        val category2 = CategoryDocument("DELETE_CATEGORY_TEST 2", -65536)
        val category3 = CategoryDocument("DELETE_CATEGORY_TEST 3", -65536)
    }

    @get:Rule
    var activityRule = ActivityScenarioRule(CategoryManagerActivity::class.java)

    @Before
    fun setUp() {
        getInstrumentation().runOnMainSync {
            val databaseViewModel = DatabaseViewModel.Factory(
                getInstrumentation().context.resources
            ).create()

            databaseViewModel.clearDatabase()
            databaseViewModel.upsertCategory(category1)
            databaseViewModel.upsertCategory(category2)
            databaseViewModel.upsertCategory(category3)
        }
    }

    @Test
    fun categoryIsDeleted() {
        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))
            .check(matches(hasDescendant(withText(category2.name))))
            .check(matches(hasDescendant(withText(category3.name))))

        onView(
            allOf(withId(R.id.item_category), hasDescendant(withText(category2.name)))
        ).perform(longClick())
        onView(withId(R.id.action_menu_delete)).perform(click())
        sleep(200)

        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))
            .check(matches(not(hasDescendant(withText(category2.name)))))
            .check(matches(hasDescendant(withText(category3.name))))
    }

    @Test
    fun multipleCategoriesAreDeleted() {
        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))
            .check(matches(hasDescendant(withText(category2.name))))
            .check(matches(hasDescendant(withText(category3.name))))

        onView(
            allOf(withId(R.id.item_category), hasDescendant(withText(category1.name)))
        ).perform(longClick())
        onView(
            allOf(withId(R.id.item_category), hasDescendant(withText(category2.name)))
        ).perform(click())
        onView(withId(R.id.action_menu_delete)).perform(click())
        sleep(200)

        onView(withId(R.id.rcv_categories))
            .check(matches(not(hasDescendant(withText(category1.name)))))
            .check(matches(not(hasDescendant(withText(category2.name)))))
            .check(matches(hasDescendant(withText(category3.name))))
    }

}