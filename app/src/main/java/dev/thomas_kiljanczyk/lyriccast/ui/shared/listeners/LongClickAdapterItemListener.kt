/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LongClickAdapterItemListener<T>(
    private val listener: (T, Int, View) -> Boolean
) where T : RecyclerView.ViewHolder {

    fun execute(holder: T, position: Int, view: View): Boolean {
        return listener(holder, position, view)
    }

}