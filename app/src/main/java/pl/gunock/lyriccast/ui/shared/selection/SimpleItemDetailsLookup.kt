/*
 * Created by Tomasz Kiljanczyk on 27/01/2023, 23:20
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 27/01/2023, 22:23
 */

package pl.gunock.lyriccast.ui.shared.selection

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class SimpleItemDetailsLookup(
    private val recyclerView: RecyclerView
) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y) ?: return null
        val viewHolder = recyclerView.getChildViewHolder(view) as SelectionViewHolder<*>
        return viewHolder.getItemDetails()
    }
}
