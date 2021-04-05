/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:33 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:32 PM
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
}