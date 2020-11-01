/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 2:11 PM
 */

package pl.gunock.lyriccast.utils

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import pl.gunock.lyriccast.R

fun addSong(title: String) {
    onView(withId(R.id.fab_add))
        .perform(click())
    onView(withId(R.id.fab_view_add_song))
        .check(matches(isDisplayed()))

    onView(withId(R.id.fab_add_song))
        .perform(click())

    onView(withId(R.id.text_input_song_title))
        .perform(replaceText(title))
    Espresso.closeSoftKeyboard()

    onView(withId(R.id.button_save_song))
        .perform(click())
}

fun addSetlist(name: String) {
    onView(withId(R.id.fab_add))
        .perform(click())
    onView(withId(R.id.fab_view_add_setlist))
        .check(matches(isDisplayed()))

    onView(withId(R.id.fab_add_setlist))
        .perform(click())

    onView(withId(R.id.text_input_setlist_name))
        .perform(ViewActions.typeText(name))
    Espresso.closeSoftKeyboard()

    onView(withId(R.id.button_save_setlist))
        .perform(click())
}