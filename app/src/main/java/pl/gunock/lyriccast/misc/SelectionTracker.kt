/*
 * Created by Tomasz Kiljanczyk on 4/5/21 4:34 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 3:46 PM
 */

package pl.gunock.lyriccast.misc

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SelectionTracker<T : RecyclerView.ViewHolder>(
    private val recyclerView: RecyclerView,
    private val onSelect: (holder: T, position: Int, isLongClick: Boolean) -> Boolean
) {

    var count: Int = 0
        private set(value) {
            if (value < 0) {
                throw RuntimeException("Selection count below 0")
            }
            field = value
        }

    var countAfter: Int = 0
        private set

    private val selectedItemIds: MutableSet<Long> = mutableSetOf()

    fun reset() {
        countAfter = 0
        count = 0
        selectedItemIds.clear()
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
        if (selectedItemIds.contains(holder.itemId)) {
            if (modifySelectedItems) {
                selectedItemIds.remove(holder.itemId)
            }
            updatedCount--
        } else {
            if (modifySelectedItems) {
                selectedItemIds.add(holder.itemId)
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