/*
 * Created by Tomasz Kiljańczyk on 2/25/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/25/21 2:28 AM
 */

package pl.gunock.lyriccast.adapters.listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LongClickAdapterListener<T>(
    val mListener: (T, Int, View) -> Boolean
) where T : RecyclerView.ViewHolder {

    fun execute(holder: T, position: Int, view: View): Boolean {
        return mListener(holder, position, view)
    }

}