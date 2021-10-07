/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 13:48
 */

package pl.gunock.lyriccast.ui.shared.misc

import android.view.View
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder

class SelectionTracker<T : BaseViewHolder>(
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
            if (onSelect(holder, holder.absoluteAdapterPosition, true)) {
                view.requestFocus()
                count = countItems(holder, modifySelectedItems = true)
            }
            countAfter = count
            return@setOnLongClickListener true
        }

        holder.itemView.setOnClickListener { view ->
            focus(view)
            countAfter = countItems(holder, modifySelectedItems = false)
            if (onSelect(holder, holder.absoluteAdapterPosition, false)) {
                view.requestFocus()
                count = countItems(holder, modifySelectedItems = true)
            }
            countAfter = count
        }
    }

    private fun countItems(holder: T, modifySelectedItems: Boolean = true): Int {
        var updatedCount: Int = count
        if (holder.itemId in selectedItemIds) {
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