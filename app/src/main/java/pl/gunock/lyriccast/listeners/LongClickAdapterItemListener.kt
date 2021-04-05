/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:11 PM
 */

package pl.gunock.lyriccast.listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LongClickAdapterItemListener<T>(
    private val mListener: (T, Int, View) -> Boolean
) where T : RecyclerView.ViewHolder {

    fun execute(holder: T, position: Int, view: View): Boolean {
        return mListener(holder, position, view)
    }

}