/*
 * Created by Tomasz Kiljanczyk on 14/05/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 13/05/2021, 10:20
 */

package pl.gunock.lyriccast.tests.category_manager

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
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
class AddCategoryTest {

    private companion object {
        const val newCategoryName = "AddCategoryTest 2"
        val category1 = CategoryDocument("ADD_CATEGORY_TEST 1", -65536)
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
        }
    }

    @Test
    fun categoryIsAdded() {
        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))

        onView(withId(R.id.menu_add_category)).perform(click())

        onView(withId(R.id.tv_dialog_title))
            .check(matches(withText("Add category")))

        onView(withId(R.id.ed_category_name)).perform(replaceText(newCategoryName))
        onView(withId(R.id.btn_save_category)).perform(click())

        sleep(200)

        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))
            .check(matches(hasDescendant(withText(newCategoryName.toUpperCase()))))
    }

    @Test
    fun categoryNameAlreadyInUse() {
        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))

        onView(withId(R.id.menu_add_category)).perform(click())

        onView(withId(R.id.tv_dialog_title))
            .check(matches(withText("Add category")))

        onView(withId(R.id.ed_category_name)).perform(replaceText(category1.name))

        onView(withId(R.id.tin_category_name))
            .check(matches(hasDescendant(withText("Category name already in use"))))
    }

    @Test
    fun categoryNameIsUpperCase() {
        onView(withId(R.id.menu_add_category)).perform(click())

        onView(withId(R.id.tv_dialog_title))
            .check(matches(withText("Add category")))

        onView(withId(R.id.ed_category_name)).perform(replaceText(newCategoryName))

        onView(withId(R.id.ed_category_name))
            .check(matches(withText(newCategoryName.toUpperCase())))
    }

}