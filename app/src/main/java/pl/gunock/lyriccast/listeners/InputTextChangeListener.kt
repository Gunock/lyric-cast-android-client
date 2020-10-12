/*
 * Created by Tomasz Kiljańczyk on 10/12/20 10:37 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/12/20 9:27 PM
 */

package pl.gunock.lyriccast.listeners

import android.text.Editable
import android.text.TextWatcher

class InputTextChangeListener(private val mListener: (newText: String) -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        mListener(s.toString())
    }

}