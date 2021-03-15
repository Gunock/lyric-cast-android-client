/*
 * Created by Tomasz Kiljańczyk on 3/15/21 1:45 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 1:45 AM
 */

package pl.gunock.lyriccast.misc

import android.view.View
import androidx.lifecycle.Observer

class VisibilityObserver(private val view: View) : Observer<Boolean> {

    override fun onChanged(value: Boolean) {
        if (value) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

}