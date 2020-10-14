/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/13/20 10:20 PM
 */

package pl.gunock.lyriccast.listeners

import android.view.View
import android.widget.AdapterView


class SpinnerItemSelectedListener(
    private val mListener: (view: View?, position: Int) -> Unit
) : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mListener(view, position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }


}