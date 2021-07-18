/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:19
 */

package pl.gunock.lyriccast.tests.e2e.category_manager

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
class EditCategoryTest {

    private companion object {
        const val editedCategoryName = "EDIT_CATEGORY_TEST 2 EDITED"
        val category1 = CategoryDocument("EDIT_CATEGORY_TEST 1", -65536)
        val category2 = CategoryDocument("EDIT_CATEGORY_TEST 2", -65536)
        val category3 = CategoryDocument("EDIT_CATEGORY_TEST 3", -65536)
    }

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

        ActivityScenario.launch(CategoryManagerActivity::class.java)
    }

    @Test
    fun categoryIsEdited() {
        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))
            .check(matches(hasDescendant(withText(category2.name))))
            .check(matches(hasDescendant(withText(category3.name))))

        onView(
            allOf(withId(R.id.item_category), hasDescendant(withText(category2.name)))
        ).perform(longClick())
        sleep(200)
        onView(withId(R.id.action_menu_edit)).perform(click())

        onView(withId(R.id.tv_dialog_title))
            .check(matches(withText("Edit category")))

        onView(withId(R.id.ed_category_name)).perform(replaceText(editedCategoryName))
        onView(withId(R.id.btn_save_category)).perform(click())

        sleep(200)

        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))
            .check(matches(not(hasDescendant(withText(category2.name)))))
            .check(matches(hasDescendant(withText(category3.name))))
            .check(matches(hasDescendant(withText(editedCategoryName))))
    }

}