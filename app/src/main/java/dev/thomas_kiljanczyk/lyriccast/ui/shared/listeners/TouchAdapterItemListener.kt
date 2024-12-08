/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.listeners

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun interface TouchAdapterItemListener<T> where T : RecyclerView.ViewHolder {

    fun execute(holder: T, view: View, event: MotionEvent): Boolean

}