/*
 * Created by Tomasz Kiljańczyk on 3/12/21 4:03 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/12/21 3:56 PM
 */

package pl.gunock.lyriccast.helpers

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

object KeyboardHelper {
    fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager? =
            ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
        inputMethodManager?.hideSoftInputFromWindow(
            view.applicationWindowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    fun showKeyboard(view: View) {
        val inputMethodManager: InputMethodManager? =
            ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
        inputMethodManager?.toggleSoftInputFromWindow(
            view.applicationWindowToken,
            InputMethodManager.SHOW_IMPLICIT,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}