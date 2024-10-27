/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 23:28
 */

package pl.gunock.lyriccast.tests.ui.category_manager

import android.graphics.Color
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
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
import pl.gunock.lyriccast.shared.BaseHiltTest
import pl.gunock.lyriccast.shared.CustomEspresso.touchscreenLongClick
import pl.gunock.lyriccast.shared.retryWithTimeout
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import javax.inject.Inject

@HiltAndroidTest
@LargeTest
class EditCategoryTest : BaseHiltTest() {

    private companion object {
        const val EDITED_CATEGORY_NAME = "EDIT_CATEGORY_TEST 2 EDITED"
        val category1 = Category("EDIT_CATEGORY_TEST 1", Color.RED, "1")
        val category2 = Category("EDIT_CATEGORY_TEST 2", Color.RED, "2")
        val category3 = Category("EDIT_CATEGORY_TEST 3", Color.RED, "3")
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
            categoriesRepository.upsertCategory(category2)
            categoriesRepository.upsertCategory(category3)
        }
    }

    @Test
    fun categoryIsEdited() {
        retryWithTimeout {
            onView(withId(R.id.rcv_categories))
                .check(matches(hasDescendant(withText(category1.name))))
                .check(matches(hasDescendant(withText(category2.name))))
                .check(matches(hasDescendant(withText(category3.name))))
        }

        onView(
            allOf(withId(R.id.item_category), hasDescendant(withText(category2.name)))
        ).perform(touchscreenLongClick())

        onView(withId(R.id.action_menu_edit)).perform(click())

        onView(withId(com.google.android.material.R.id.alertTitle))
            .check(matches(withText("Edit category")))

        onView(withId(R.id.ed_category_name)).perform(replaceText(EDITED_CATEGORY_NAME))
        onView(withId(android.R.id.button1)).perform(click())

        retryWithTimeout {
            onView(withId(R.id.rcv_categories))
                .check(matches(hasDescendant(withText(category1.name))))
                .check(matches(not(hasDescendant(withText(category2.name)))))
                .check(matches(hasDescendant(withText(category3.name))))
                .check(matches(hasDescendant(withText(EDITED_CATEGORY_NAME))))
        }
    }

}