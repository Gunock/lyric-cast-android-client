/*
 * Created by Tomasz Kiljańczyk on 3/15/21 3:53 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 3:53 AM
 */

package pl.gunock.lyriccast.misc

import android.view.HapticFeedbackConstants
import androidx.recyclerview.widget.RecyclerView

class SelectionTracker<T : RecyclerView.ViewHolder>(
    private val recyclerView: RecyclerView,
    private val onSelect: (holder: T, position: Int, isLongClick: Boolean) -> Boolean
) {

    init {
        if (recyclerView.adapter?.hasStableIds() == false) {
            throw IllegalArgumentException("RecyclerView does not have stable ids")
        }
    }

    private var _count: Int = 0

    var count: Int
        get() = _count
        private set(value) {
            if (value < 0) {
                throw RuntimeException("Selection count below 0")
            }

            countAfter = value
            _count = value
        }

    var countAfter: Int = 0
        private set

    private val selectedItems: MutableSet<Long> = mutableSetOf()

    fun reset() {
        countAfter = 0
        count = 0
        selectedItems.clear()
    }

    fun attach(holder: T) {
        holder.itemView.setOnLongClickListener {
            countAfter = countItems(holder, modifySelectedItems = false)
            if (onSelect(holder, holder.adapterPosition, true)) {
                count = countItems(holder, modifySelectedItems = true)
            }
            return@setOnLongClickListener true
        }

        holder.itemView.setOnClickListener {
            countAfter = countItems(holder, modifySelectedItems = false)
            if (onSelect(holder, holder.adapterPosition, false)) {
                recyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                count = countItems(holder, modifySelectedItems = true)
            }
        }
    }

    private fun countItems(holder: T, modifySelectedItems: Boolean = true): Int {
        if (selectedItems.contains(holder.itemId)) {
            if (modifySelectedItems) {
                selectedItems.remove(holder.itemId)
            }
            return count - 1
        } else {
            if (modifySelectedItems) {
                selectedItems.add(holder.itemId)
            }
            return count + 1
        }
    }

}