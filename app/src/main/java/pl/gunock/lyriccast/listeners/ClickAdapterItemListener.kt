/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 10:59 PM
 */

package pl.gunock.lyriccast.listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun interface ClickAdapterItemListener<T> where T : RecyclerView.ViewHolder {

    fun execute(holder: T, position: Int, view: View)

}