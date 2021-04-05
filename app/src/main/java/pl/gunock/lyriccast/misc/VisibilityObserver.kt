/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:12 PM
 */

package pl.gunock.lyriccast.misc

import android.view.View
import androidx.lifecycle.Observer

class VisibilityObserver(
    private val mView: View,
    private val mReversed: Boolean = false
) : Observer<Boolean> {

    override fun onChanged(value: Boolean) {
        if (value.xor(mReversed)) {
            mView.visibility = View.VISIBLE
        } else {
            mView.visibility = View.GONE
        }
    }

}