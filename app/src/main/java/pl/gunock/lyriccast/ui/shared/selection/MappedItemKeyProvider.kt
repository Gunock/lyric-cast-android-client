/*
 * Created by Tomasz Kiljanczyk on 27/01/2023, 23:20
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 27/01/2023, 23:11
 */

package pl.gunock.lyriccast.ui.shared.selection

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView

class MappedItemKeyProvider(
    private val recyclerView: RecyclerView
) : ItemKeyProvider<Long>(SCOPE_MAPPED) {
    override fun getKey(position: Int): Long? {
        return recyclerView.adapter?.getItemId(position)
    }

    override fun getPosition(key: Long): Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }

}