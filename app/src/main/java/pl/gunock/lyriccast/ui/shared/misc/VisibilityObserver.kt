/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:16
 */

package pl.gunock.lyriccast.ui.shared.misc

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