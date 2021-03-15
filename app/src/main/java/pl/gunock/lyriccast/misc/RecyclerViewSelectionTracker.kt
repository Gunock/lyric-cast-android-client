/*
 * Created by Tomasz Kiljańczyk on 3/15/21 2:57 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 2:18 AM
 */

package pl.gunock.lyriccast.misc

import android.view.HapticFeedbackConstants
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener

class RecyclerViewSelectionTracker<T : RecyclerView.ViewHolder>(
    private val recyclerView: RecyclerView,
    private val onSelect: (holder: T, position: Int, isLongClick: Boolean) -> Boolean
) {

    var count: Int = 0
        private set

    var countBefore: Int = 0
        private set

    private val selectedItems: MutableSet<Long> = mutableSetOf()

    private val onLongClickListener =
        LongClickAdapterItemListener { holder: T, position: Int, _ ->
            countItems(holder)
            onSelect(holder, position, true)
            return@LongClickAdapterItemListener true
        }

    private val onClickListener =
        ClickAdapterItemListener { holder: T, position: Int, _ ->
            countItems(holder)
            if (onSelect(holder, position, false)) {
                recyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }

    fun reset() {
        countBefore = count
        count = 0
        selectedItems.clear()
    }

    fun attach(holder: T) {
        holder.itemView.setOnLongClickListener { view ->
            onLongClickListener.execute(holder, holder.adapterPosition, view)
        }

        holder.itemView.setOnClickListener { view ->
            onClickListener.execute(holder, holder.adapterPosition, view)
        }
    }

    private fun countItems(holder: T) {
        countBefore = count
        if (selectedItems.contains(holder.itemId)) {
            selectedItems.remove(holder.itemId)
            count--
        } else {
            selectedItems.add(holder.itemId)
            count++
        }
    }

}