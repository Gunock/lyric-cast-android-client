/*
 * Created by Tomasz Kiljańczyk on 2/25/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/25/21 2:33 AM
 */

package pl.gunock.lyriccast.adapters.listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ClickAdapterListener<T>(
    val mListener: (T, Int, View) -> Unit
) where T : RecyclerView.ViewHolder {

    fun execute(holder: T, position: Int, view: View) {
        mListener(holder, position, view)
    }

}