/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.tests.integration.category_manager

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
import dagger.hilt.android.testing.HiltAndroidTest
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.modules.FakeAppModule
import dev.thomas_kiljanczyk.lyriccast.shared.BaseHiltTest
import dev.thomas_kiljanczyk.lyriccast.shared.retryWithTimeout
import dev.thomas_kiljanczyk.lyriccast.ui.category_manager.CategoryManagerActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
class CategoryNameValidationTest : BaseHiltTest() {

    private companion object {
        const val NEW_CATEGORY_NAME = "NameValidationTest 2"
        const val NEW_CATEGORY_LONG_NAME = "NAME_VALIDATION_TEST 2 VERY LONG NAME VERY SO LONG"
        val category1 = Category("NAME_VALIDATION_TEST 1", Color.RED)
    }

    @get:Rule(order = 1)
    var activityRule = ActivityScenarioRule(CategoryManagerActivity::class.java)

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            categoriesRepository.upsertCategory(category1)
        }
    }

    @After
    fun cleanup() {
        FakeAppModule.cleanupDataStore()
    }

    @Test
    fun categoryNameAlreadyInUse() {
        retryWithTimeout {
            onView(withId(R.id.rcv_categories))
                .check(matches(hasDescendant(ViewMatchers.withText(category1.name))))
        }

        onView(withId(R.id.menu_add_category)).perform(ViewActions.click())

        onView(withId(com.google.android.material.R.id.alertTitle))
            .check(matches(ViewMatchers.withText("Add category")))

        onView(withId(R.id.ed_category_name))
            .perform(ViewActions.replaceText(category1.name))

        onView(withId(R.id.tin_category_name))
            .check(matches(hasDescendant(ViewMatchers.withText("Category name already in use"))))
    }

    @Test
    fun categoryNameIsUpperCase() {
        onView(withId(R.id.menu_add_category)).perform(ViewActions.click())

        onView(withId(com.google.android.material.R.id.alertTitle))
            .check(matches(ViewMatchers.withText("Add category")))

        onView(withId(R.id.ed_category_name))
            .perform(ViewActions.replaceText(NEW_CATEGORY_NAME))

        val newCategoryNameUppercase = NEW_CATEGORY_NAME.uppercase()
        onView(withId(R.id.ed_category_name))
            .check(matches(ViewMatchers.withText(newCategoryNameUppercase)))
    }

    @Test
    fun categoryNameLengthIsLimited() {
        val maxNameLength = getInstrumentation().targetContext
            .resources
            .getInteger(R.integer.ed_max_length_category_name)

        onView(withId(R.id.menu_add_category)).perform(ViewActions.click())

        onView(ViewMatchers.withText("Add category"))
            .check(matches(ViewMatchers.withText("Add category")))

        onView(withId(R.id.ed_category_name))
            .perform(ViewActions.replaceText(NEW_CATEGORY_LONG_NAME))

        val limitedCategoryName = NEW_CATEGORY_LONG_NAME.substring(0, maxNameLength)
        onView(withId(R.id.ed_category_name))
            .check(matches(ViewMatchers.withText(limitedCategoryName)))
    }

}