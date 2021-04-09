/*
 * Created by Tomasz Kiljanczyk on 4/9/21 11:51 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/9/21 11:03 PM
 */

package pl.gunock.lyriccast.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.misc.VisibilityObserver
import pl.gunock.lyriccast.models.SongItem

class SetlistSongItemsAdapter(
    private val mContext: Context,
    private val mSongItems: MutableList<SongItem>,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    private val mSelectionTracker: SelectionTracker<ViewHolder>?,
    private val mOnHandleTouchListener: TouchAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<SetlistSongItemsAdapter.ViewHolder>() {

    val songItems: List<SongItem> get() = mSongItems

    private var availableId: Long = 0L

    init {
        mSongItems.forEach { it.id = availableId++ }

        setHasStableIds(true)
    }

    fun resetSelection() {
        mSongItems.forEach { it.isSelected.value = false }
        mSelectionTracker?.reset()
    }

    fun moveItem(from: Int, to: Int) {
        val item = mSongItems.removeAt(from)
        mSongItems.add(to, item)
    }

    fun duplicateSelectedItem() {
        val selectedItemIndex = mSongItems.indexOfFirst { item -> item.isSelected.value!! }
        val selectedItem = mSongItems[selectedItemIndex].copy(id = availableId++)
        selectedItem.isSelected.value = false

        mSongItems.add(selectedItemIndex + 1, selectedItem)
        notifyItemInserted(selectedItemIndex + 1)
    }

    fun removeSelectedItems() {
        @Suppress("ControlFlowWithEmptyBody")
        while (deleteSelectedItem()) {
        }
    }

    private fun deleteSelectedItem(): Boolean {
        val selectedItemIndex = mSongItems.indexOfFirst { item -> item.isSelected.value!! }
        if (selectedItemIndex == -1) {
            return false
        }
        mSongItems.removeAt(selectedItemIndex)
        notifyItemRemoved(selectedItemIndex)
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setlist_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        val item = mSongItems[position]
        return item.id
    }

    override fun getItemCount() = mSongItems.size

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val mCheckBox: CheckBox = itemView.findViewById(R.id.chk_item_song)
        private val mHandleView: View = itemView.findViewById(R.id.imv_handle)
        private val mTitleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)

        fun bind(position: Int) {
            val item = mSongItems[position]
            mSelectionTracker?.attach(this)
            setupListeners()

            item.isSelected.observe(mContext.getLifecycleOwner()!!) {
                mCheckBox.isChecked = it
            }

            showCheckBox
                .observe(mContext.getLifecycleOwner()!!, VisibilityObserver(mHandleView, true))
            showCheckBox.observe(mContext.getLifecycleOwner()!!, VisibilityObserver(mCheckBox))

            mTitleTextView.text = item.song.title
        }

        private fun setupListeners() {
            @SuppressLint("ClickableViewAccessibility")
            if (mOnHandleTouchListener != null) {
                mHandleView.setOnTouchListener { view, event ->
                    mOnHandleTouchListener.execute(this, view, event)
                }
            }
        }
    }
}