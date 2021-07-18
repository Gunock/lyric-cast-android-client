/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 11:34
 */

package pl.gunock.lyriccast.extensions

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