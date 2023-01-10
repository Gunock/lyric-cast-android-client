/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.ui.category_manager

import android.graphics.Color
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
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
@LargeTest
class DeleteCategoryTest {

    private companion object {
        val category1 = Category("DELETE_CATEGORY_TEST 1", Color.RED, "1")
        val category2 = Category("DELETE_CATEGORY_TEST 2", Color.RED, "2")
        val category3 = Category("DELETE_CATEGORY_TEST 3", Color.RED, "3")
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule = ActivityScenarioRule(CategoryManagerActivity::class.java)

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    @Before
    fun setUp() {
        hiltRule.inject()

        runBlocking {
            categoriesRepository.upsertCategory(category1)
            categoriesRepository.upsertCategory(category2)
            categoriesRepository.upsertCategory(category3)
        }
        sleep(100)
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

        sleep(100)

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

        sleep(100)

        onView(withId(R.id.rcv_categories))
            .check(matches(not(hasDescendant(withText(category1.name)))))
            .check(matches(not(hasDescendant(withText(category2.name)))))
            .check(matches(hasDescendant(withText(category3.name))))
    }

}