/*
 * Created by Tomasz Kiljanczyk on 08/01/2023, 23:50
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 08/01/2023, 23:37
 */

package pl.gunock.lyriccast

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewInteraction
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

}