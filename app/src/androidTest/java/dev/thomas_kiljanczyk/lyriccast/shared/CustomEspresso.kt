/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.shared

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import org.hamcrest.Matcher
import java.lang.Thread.sleep

object CustomEspresso {

    fun waitForView(viewMatcher: Matcher<View>, millis: Int): ViewInteraction {
        val endTime = System.currentTimeMillis() + millis

        do {
            try {
                onView(viewMatcher)
            } catch (_: NoMatchingViewException) {
                sleep(10)
            }
        } while (System.currentTimeMillis() < endTime)

        return onView(viewMatcher)
    }

    fun touchscreenClick(): ViewAction {
        return ViewActions.actionWithAssertions(
            GeneralClickAction(
                Tap.SINGLE,
                GeneralLocation.CENTER,
                Press.FINGER,
                InputDevice.SOURCE_TOUCHSCREEN,
                MotionEvent.BUTTON_PRIMARY
            )
        )
    }

    fun touchscreenLongClick(): ViewAction {
        return ViewActions.actionWithAssertions(
            GeneralClickAction(
                Tap.LONG,
                GeneralLocation.CENTER,
                Press.FINGER,
                InputDevice.SOURCE_TOUCHSCREEN,
                MotionEvent.BUTTON_PRIMARY
            )
        )
    }

}