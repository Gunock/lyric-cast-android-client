/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/27/21 11:36 PM
 */

package pl.gunock.lyriccast.misc

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SelectionTracker<T : RecyclerView.ViewHolder>(
    private val recyclerView: RecyclerView,
    private val onSelect: (holder: T, position: Int, isLongClick: Boolean) -> Boolean
) {

    private var _count: Int = 0

    var count: Int
        get() = _count
        private set(value) {
            if (value < 0) {
                throw RuntimeException("Selection count below 0")
            }

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
        holder.itemView.setOnLongClickListener { view ->
            focus(view)
            countAfter = countItems(holder, modifySelectedItems = false)
            if (onSelect(holder, holder.adapterPosition, true)) {
                view.requestFocus()
                count = countItems(holder, modifySelectedItems = true)
            }
            countAfter = count
            return@setOnLongClickListener true
        }

        holder.itemView.setOnClickListener { view ->
            focus(view)
            countAfter = countItems(holder, modifySelectedItems = false)
            if (onSelect(holder, holder.adapterPosition, false)) {
                view.requestFocus()
                recyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                count = countItems(holder, modifySelectedItems = true)
            }
            countAfter = count
        }
    }

    private fun countItems(holder: T, modifySelectedItems: Boolean = true): Int {
        var updatedCount: Int = count
        if (selectedItems.contains(holder.itemId)) {
            if (modifySelectedItems) {
                selectedItems.remove(holder.itemId)
            }
            updatedCount--
        } else {
            if (modifySelectedItems) {
                selectedItems.add(holder.itemId)
            }
            updatedCount++
        }
        return updatedCount
    }

    private fun focus(view: View) {
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.isFocusableInTouchMode = false
    }
}