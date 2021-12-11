/*
 * Created by Tomasz Kiljanczyk on 12/11/2021, 18:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/11/2021, 17:59
 */

package pl.gunock.lyriccast.ui.shared.listeners

import android.view.View
import android.widget.AdapterView


class ItemSelectedSpinnerListener(
    private val listener: (view: View, position: Int) -> Unit
) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        listener(view, position)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }


}