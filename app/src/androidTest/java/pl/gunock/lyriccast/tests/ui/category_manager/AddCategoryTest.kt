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
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import pl.gunock.lyriccast.shared.CustomEspresso.waitForView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.shared.BaseHiltTest
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity


@HiltAndroidTest
@LargeTest
class AddCategoryTest: BaseHiltTest() {

    private companion object {
        const val newCategoryName = "AddCategoryTest 2"
    }

    @get:Rule(order = 1)
    var activityRule = ActivityScenarioRule(CategoryManagerActivity::class.java)

    @Test
    fun categoryIsAdded() {
        val colorName = getInstrumentation().targetContext
            .resources
            .getStringArray(R.array.category_color_names)[1]


        onView(withId(R.id.menu_add_category))
            .perform(click())

        onView(withId(com.google.android.material.R.id.alertTitle))
            .check(matches(withText("Add category")))

        onView(withId(R.id.ed_category_name))
            .perform(replaceText(newCategoryName))

        onView(withId(R.id.dropdown_color))
            .perform(click())

        onView(allOf(withId(R.id.text_color_name), withText(colorName)))
            .perform()

        waitForView(allOf(withId(R.id.text_color_name), withText(colorName)), 100)
            .inRoot(isPlatformPopup())
            .perform(click())

        onView(withId(android.R.id.button1)).perform(click())

        onView(withId(R.id.rcv_categories))
            .check(matches(hasDescendant(withText(newCategoryName.uppercase()))))
    }
}