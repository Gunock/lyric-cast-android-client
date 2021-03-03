/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:03 PM
 */

package pl.gunock.lyriccast.helpers

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

object KeyboardHelper {
    fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager? =
            ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
        inputMethodManager?.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }
}