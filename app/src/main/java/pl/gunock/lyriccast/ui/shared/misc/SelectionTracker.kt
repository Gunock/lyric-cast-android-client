/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:16
 */

package pl.gunock.lyriccast.ui.shared.misc

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SelectionTracker<T : RecyclerView.ViewHolder>(
    private val mOnSelect: (holder: T, position: Int, isLongClick: Boolean) -> Boolean
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

    private val mSelectedItemIds: MutableSet<Long> = mutableSetOf()

    fun reset() {
        countAfter = 0
        count = 0
        mSelectedItemIds.clear()
    }

    fun attach(holder: T) {
        holder.itemView.setOnLongClickListener { view ->
            focus(view)
            countAfter = countItems(holder, modifySelectedItems = false)
            if (mOnSelect(holder, holder.absoluteAdapterPosition, true)) {
                view.requestFocus()
                count = countItems(holder, modifySelectedItems = true)
            }
            countAfter = count
            return@setOnLongClickListener true
        }

        holder.itemView.setOnClickListener { view ->
            focus(view)
            countAfter = countItems(holder, modifySelectedItems = false)
            if (mOnSelect(holder, holder.absoluteAdapterPosition, false)) {
                view.requestFocus()
                count = countItems(holder, modifySelectedItems = true)
            }
            countAfter = count
        }
    }

    private fun countItems(holder: T, modifySelectedItems: Boolean = true): Int {
        var updatedCount: Int = count
        if (mSelectedItemIds.contains(holder.itemId)) {
            if (modifySelectedItems) {
                mSelectedItemIds.remove(holder.itemId)
            }
            updatedCount--
        } else {
            if (modifySelectedItems) {
                mSelectedItemIds.add(holder.itemId)
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