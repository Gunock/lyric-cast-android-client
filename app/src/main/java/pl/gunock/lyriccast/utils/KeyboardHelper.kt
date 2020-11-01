/*
 * Created by Tomasz Kiljańczyk on 10/20/20 10:55 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/20/20 10:54 PM
 */

package pl.gunock.lyriccast.utils

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