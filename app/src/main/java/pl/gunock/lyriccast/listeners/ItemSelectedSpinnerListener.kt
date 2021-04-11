/*
 * Created by Tomasz Kiljanczyk on 4/11/21 2:05 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 12:43 AM
 */

package pl.gunock.lyriccast.listeners

import android.view.View
import android.widget.AdapterView


class ItemSelectedSpinnerListener(
    private val mListener: (view: View, position: Int) -> Unit
) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        mListener(view, position)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }


}