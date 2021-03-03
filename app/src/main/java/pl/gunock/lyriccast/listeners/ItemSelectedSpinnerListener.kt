/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:00 PM
 */

package pl.gunock.lyriccast.listeners

import android.view.View
import android.widget.AdapterView


class ItemSelectedSpinnerListener(
    private val mListener: (view: View?, position: Int) -> Unit
) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mListener(view, position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }


}