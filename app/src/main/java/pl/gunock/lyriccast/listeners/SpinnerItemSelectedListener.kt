/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:16 PM
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