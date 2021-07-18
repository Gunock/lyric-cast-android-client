/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:19
 */

package pl.gunock.lyriccast.tests.integration.category_manager

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import java.util.*

@RunWith(AndroidJUnit4::class)
class CategoryNameValidationTest {

    private companion object {
        const val newCategoryName = "NameValidationTest 2"
        const val newCategoryLongName = "NAME_VALIDATION_TEST 2 VERY LONG NAME VERY SO LONG"
        val category1 = CategoryDocument("NAME_VALIDATION_TEST 1", -65536)
    }

    @Before
    fun setUp() {
        getInstrumentation().runOnMainSync {
            val databaseViewModel = DatabaseViewModel.Factory(
                getInstrumentation().context.resources
            ).create()

            databaseViewModel.clearDatabase()
            databaseViewModel.upsertCategory(category1)
        }

        ActivityScenario.launch(CategoryManagerActivity::class.java)
    }

    @Test
    fun categoryNameAlreadyInUse() {
        Espresso.onView(ViewMatchers.withId(R.id.rcv_categories))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.hasDescendant(
                        ViewMatchers.withText(category1.name)
                    )
                )
            )

        Espresso.onView(ViewMatchers.withId(R.id.menu_add_category)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.tv_dialog_title))
            .check(ViewAssertions.matches(ViewMatchers.withText("Add category")))

        Espresso.onView(ViewMatchers.withId(R.id.ed_category_name))
            .perform(ViewActions.replaceText(category1.name))

        Espresso.onView(ViewMatchers.withId(R.id.tin_category_name))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("Category name already in use"))))
    }

    @Test
    fun categoryNameIsUpperCase() {
        Espresso.onView(ViewMatchers.withId(R.id.menu_add_category)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.tv_dialog_title))
            .check(ViewAssertions.matches(ViewMatchers.withText("Add category")))

        Espresso.onView(ViewMatchers.withId(R.id.ed_category_name))
            .perform(ViewActions.replaceText(newCategoryName))

        val newCategoryNameUppercase = newCategoryName.uppercase(Locale.getDefault())
        Espresso.onView(ViewMatchers.withId(R.id.ed_category_name))
            .check(ViewAssertions.matches(ViewMatchers.withText(newCategoryNameUppercase)))
    }

    @Test
    fun categoryNameLengthIsLimited() {
        val maxNameLength = getInstrumentation().targetContext
            .resources
            .getInteger(R.integer.ed_max_length_category_name)

        Espresso.onView(ViewMatchers.withId(R.id.menu_add_category)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.tv_dialog_title))
            .check(ViewAssertions.matches(ViewMatchers.withText("Add category")))

        Espresso.onView(ViewMatchers.withId(R.id.ed_category_name))
            .perform(ViewActions.replaceText(newCategoryLongName))

        val limitedCategoryName = newCategoryLongName.substring(0, maxNameLength)
        Espresso.onView(ViewMatchers.withId(R.id.ed_category_name))
            .check(ViewAssertions.matches(ViewMatchers.withText(limitedCategoryName)))
    }

}