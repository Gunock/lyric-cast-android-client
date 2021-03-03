/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 10:59 PM
 */

package pl.gunock.lyriccast.listeners

import android.text.Editable
import android.text.TextWatcher

class InputTextChangedListener(
    private val mListener: (newText: String) -> Unit
) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        mListener(s.toString())
    }

}