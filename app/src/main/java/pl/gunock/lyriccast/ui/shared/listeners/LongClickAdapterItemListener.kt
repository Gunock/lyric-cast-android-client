/*
 * Created by Tomasz Kiljanczyk on 12/11/2021, 18:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/11/2021, 17:59
 */

package pl.gunock.lyriccast.ui.shared.listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LongClickAdapterItemListener<T>(
    private val listener: (T, Int, View) -> Boolean
) where T : RecyclerView.ViewHolder {

    fun execute(holder: T, position: Int, view: View): Boolean {
        return listener(holder, position, view)
    }

}