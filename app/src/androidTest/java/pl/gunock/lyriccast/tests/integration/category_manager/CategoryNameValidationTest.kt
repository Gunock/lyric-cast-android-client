/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.integration.category_manager

import android.graphics.Color
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import java.lang.Thread.sleep
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class CategoryNameValidationTest {

    private companion object {
        const val newCategoryName = "NameValidationTest 2"
        const val newCategoryLongName = "NAME_VALIDATION_TEST 2 VERY LONG NAME VERY SO LONG"
        val category1 = Category("NAME_VALIDATION_TEST 1", Color.RED)
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule = ActivityScenarioRule(CategoryManagerActivity::class.java)

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    @Before
    fun setup() {
        hiltRule.inject()

        runBlocking {
            categoriesRepository.upsertCategory(category1)
        }
        sleep(100)
    }

    @Test
    fun categoryNameAlreadyInUse() {
        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(ViewMatchers.withText(category1.name))))

        onView(withId(R.id.menu_add_category)).perform(ViewActions.click())

        onView(withId(R.id.tv_dialog_title))
            .check(matches(ViewMatchers.withText("Add category")))

        onView(withId(R.id.ed_category_name))
            .perform(ViewActions.replaceText(category1.name))

        onView(withId(R.id.tin_category_name))
            .check(matches(hasDescendant(ViewMatchers.withText("Category name already in use"))))
    }

    @Test
    fun categoryNameIsUpperCase() {
        onView(withId(R.id.menu_add_category)).perform(ViewActions.click())

        onView(withId(R.id.tv_dialog_title))
            .check(matches(ViewMatchers.withText("Add category")))

        onView(withId(R.id.ed_category_name))
            .perform(ViewActions.replaceText(newCategoryName))

        val newCategoryNameUppercase = newCategoryName.uppercase()
        onView(withId(R.id.ed_category_name))
            .check(matches(ViewMatchers.withText(newCategoryNameUppercase)))
    }

    @Test
    fun categoryNameLengthIsLimited() {
        val maxNameLength = getInstrumentation().targetContext
            .resources
            .getInteger(R.integer.ed_max_length_category_name)

        onView(withId(R.id.menu_add_category)).perform(ViewActions.click())

        onView(withId(R.id.tv_dialog_title))
            .check(matches(ViewMatchers.withText("Add category")))

        onView(withId(R.id.ed_category_name))
            .perform(ViewActions.replaceText(newCategoryLongName))

        val limitedCategoryName = newCategoryLongName.substring(0, maxNameLength)
        onView(withId(R.id.ed_category_name))
            .check(matches(ViewMatchers.withText(limitedCategoryName)))
    }

}