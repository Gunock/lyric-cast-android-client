/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.integration.category_manager

import android.graphics.Color
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.RepositoryFactory
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity

class CategoryNameValidationTest {

    private companion object {
        const val newCategoryName = "NameValidationTest 2"
        const val newCategoryLongName = "NAME_VALIDATION_TEST 2 VERY LONG NAME VERY SO LONG"
        val category1 = Category("NAME_VALIDATION_TEST 1", Color.RED)
    }

    @Before
    fun setUp() {
        val categoriesRepository = RepositoryFactory.createCategoriesRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )

        runBlocking {
            categoriesRepository.upsertCategory(category1)
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

        val newCategoryNameUppercase = newCategoryName.uppercase()
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