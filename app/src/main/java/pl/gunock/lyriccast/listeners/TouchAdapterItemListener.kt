/*
 * Created by Tomasz Kiljańczyk on 3/13/21 3:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/13/21 3:19 PM
 */

package pl.gunock.lyriccast.listeners

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun interface TouchAdapterItemListener<T> where T : RecyclerView.ViewHolder {

    fun execute(holder: T, view: View, event: MotionEvent): Boolean

}