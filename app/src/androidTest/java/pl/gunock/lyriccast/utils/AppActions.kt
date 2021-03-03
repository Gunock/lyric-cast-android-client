/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:55 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:26 PM
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
    onView(withId(R.id.lns_fab_add_song))
        .check(matches(isDisplayed()))

    onView(withId(R.id.fab_add_song))
        .perform(click())

    onView(withId(R.id.tin_song_title))
        .perform(replaceText(title))
    Espresso.closeSoftKeyboard()

    onView(withId(R.id.btn_save_song))
        .perform(click())
}

fun addSetlist(name: String) {
    onView(withId(R.id.fab_add))
        .perform(click())
    onView(withId(R.id.lns_fab_add_setlist))
        .check(matches(isDisplayed()))

    onView(withId(R.id.fab_add_setlist))
        .perform(click())

    onView(withId(R.id.tin_setlist_name))
        .perform(ViewActions.typeText(name))
    Espresso.closeSoftKeyboard()

    onView(withId(R.id.btn_save_setlist))
        .perform(click())
}