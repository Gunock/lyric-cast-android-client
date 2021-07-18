/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.e2e.category_manager

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.models.mongo.CategoryDocument
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import java.lang.Thread.sleep
import java.util.*


@RunWith(AndroidJUnit4::class)
class AddCategoryTest {

    private companion object {
        const val newCategoryName = "AddCategoryTest 2"
        val newCategoryNameUppercase = newCategoryName.uppercase(Locale.getDefault())
        val category1 = CategoryDocument("ADD_CATEGORY_TEST 1", -65536)
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
    fun categoryIsAdded() {
        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))

        onView(withId(R.id.menu_add_category)).perform(click())

        onView(withId(R.id.tv_dialog_title))
            .check(matches(withText("Add category")))

        onView(withId(R.id.ed_category_name)).perform(replaceText(newCategoryName))

        onView(withId(R.id.spn_category_color)).perform(click())
        sleep(400)

        val colorName = getInstrumentation().targetContext
            .resources
            .getStringArray(R.array.category_color_names)[1]

        onView(allOf(withId(R.id.tv_spinner_color_name), withText(colorName)))
            .inRoot(isPlatformPopup())
            .perform(click())

        onView(withId(R.id.btn_save_category)).perform(click())

        sleep(200)

        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(category1.name))))
            .check(matches(hasDescendant(withText(newCategoryNameUppercase))))

    }

}