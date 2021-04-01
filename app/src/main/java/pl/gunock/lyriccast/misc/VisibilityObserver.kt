/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/30/21 11:28 PM
 */

package pl.gunock.lyriccast.misc

import android.view.View
import androidx.lifecycle.Observer

class VisibilityObserver(
    private val view: View,
    private val reversed: Boolean = false
) : Observer<Boolean> {

    override fun onChanged(value: Boolean) {
        if (value.xor(reversed)) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

}