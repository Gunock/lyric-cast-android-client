/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 13:44
 */

package pl.gunock.lyriccast.ui.shared.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

abstract class BaseViewHolder(
    itemView: View,
    private val selectionTracker: SelectionTracker<BaseViewHolder>?
) : RecyclerView.ViewHolder(itemView) {

    fun bind(position: Int) {
        selectionTracker?.attach(this)
        setUpViewHolder(position)
    }

    protected abstract fun setUpViewHolder(position: Int)

}