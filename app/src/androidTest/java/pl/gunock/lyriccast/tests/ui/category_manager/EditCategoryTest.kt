/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 23:28
 */

package pl.gunock.lyriccast.tests.ui.category_manager

import android.graphics.Color
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.RepositoryFactory
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import java.lang.Thread.sleep


class EditCategoryTest {

    private companion object {
        const val editedCategoryName = "EDIT_CATEGORY_TEST 2 EDITED"
        val category1 = Category("EDIT_CATEGORY_TEST 1", Color.RED)
        val category2 = Category("EDIT_CATEGORY_TEST 2", Color.RED)
        val category3 = Category("EDIT_CATEGORY_TEST 3", Color.RED)
    }

    @Before
    fun setUp() {
        val categoriesRepository = RepositoryFactory.createCategoriesRepository(
            RepositoryFactory.RepositoryProvider.MONGO
        )

        runBlocking {
            categoriesRepository.upsertCategory(category1)
            categoriesRepository.upsertCategory(category2)
            categoriesRepository.upsertCategory(category3)
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