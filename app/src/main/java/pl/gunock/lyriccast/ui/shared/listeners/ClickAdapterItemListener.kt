/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:16
 */

package pl.gunock.lyriccast.ui.shared.listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun interface ClickAdapterItemListener<T> where T : RecyclerView.ViewHolder {

    fun execute(holder: T, position: Int, view: View)

}