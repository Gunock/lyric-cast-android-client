/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:12 PM
 */

package pl.gunock.lyriccast.adapters.listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun interface ClickAdapterListener<T> where T : RecyclerView.ViewHolder {

    fun execute(holder: T, position: Int, view: View)

}