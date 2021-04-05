/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:12 PM
 */

package pl.gunock.lyriccast.misc

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SelectionTracker<T : RecyclerView.ViewHolder>(
    private val mRecyclerView: RecyclerView,
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
            if (mOnSelect(holder, holder.adapterPosition, true)) {
                view.requestFocus()
                count = countItems(holder, modifySelectedItems = true)
            }
            countAfter = count
            return@setOnLongClickListener true
        }

        holder.itemView.setOnClickListener { view ->
            focus(view)
            countAfter = countItems(holder, modifySelectedItems = false)
            if (mOnSelect(holder, holder.adapterPosition, false)) {
                view.requestFocus()
                mRecyclerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
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