/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 21:06
 */

package pl.gunock.lyriccast.shared.extensions

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

fun View.hideKeyboard() {
    val inputMethodManager: InputMethodManager? =
        ContextCompat.getSystemService(this.context, InputMethodManager::class.java)
    inputMethodManager?.hideSoftInputFromWindow(
        this.applicationWindowToken,
        InputMethodManager.HIDE_NOT_ALWAYS
    )
}