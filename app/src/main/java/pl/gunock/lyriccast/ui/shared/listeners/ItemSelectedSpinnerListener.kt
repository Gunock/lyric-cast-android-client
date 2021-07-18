/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:16
 */

package pl.gunock.lyriccast.ui.shared.listeners

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