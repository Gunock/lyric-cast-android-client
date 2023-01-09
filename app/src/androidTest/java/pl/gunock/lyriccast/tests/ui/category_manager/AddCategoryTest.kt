/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:38
 */

package pl.gunock.lyriccast.tests.ui.category_manager

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.tests.shared.CustomEspresso.waitForView
import pl.gunock.lyriccast.tests.shared.fakes.modules.FakeAppModule
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity


@HiltAndroidTest
@LargeTest
class AddCategoryTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule: ActivityScenarioRule<CategoryManagerActivity> =
        ActivityScenarioRule(CategoryManagerActivity::class.java)

    private companion object {
        const val newCategoryName = "AddCategoryTest 2"
    }

    @Test
    fun categoryIsAdded() {
        val colorName = getInstrumentation().targetContext
            .resources
            .getStringArray(R.array.category_color_names)[1]


        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(FakeAppModule.category1.name))))

        onView(withId(R.id.menu_add_category))
            .perform(click())

        onView(withId(R.id.tv_dialog_title))
            .check(matches(withText("Add category")))

        onView(withId(R.id.ed_category_name))
            .perform(replaceText(newCategoryName))

        onView(withId(R.id.spn_category_color))
            .perform(click())

        onView(allOf(withId(R.id.tv_spinner_color_name), withText(colorName)))
            .perform()

        waitForView(allOf(withId(R.id.tv_spinner_color_name), withText(colorName)), 1000)
            .inRoot(isPlatformPopup())
            .perform(click())

        onView(withId(R.id.btn_save_category)).perform(click())

        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(FakeAppModule.category1.name))))
            .check(matches(hasDescendant(withText(newCategoryName.uppercase()))))

    }
}