/*
 * Created by Tomasz Kiljanczyk on 4/5/21 4:34 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 4:33 PM
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
    val context: Context,
    val songItems: MutableList<SongItem>,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val selectionTracker: SelectionTracker<ViewHolder>?,
    val onHandleTouchListener: TouchAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<SetlistSongItemsAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    fun resetSelection() {
        songItems.forEach { it.isSelected.value = false }
        selectionTracker?.reset()
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
        val item = songItems[position]
        return item.song.id + position
    }

    override fun getItemCount() = songItems.size

    fun moveItem(from: Int, to: Int) {
        val item = songItems.removeAt(from)
        songItems.add(to, item)
    }

    fun duplicateSelectedItem() {
        val selectedItemIndex = songItems.indexOfFirst { item -> item.isSelected.value!! }
        val selectedItem = songItems[selectedItemIndex].copy()
        selectedItem.isSelected.value = false

        songItems.add(selectedItemIndex + 1, selectedItem)
        notifyItemInserted(selectedItemIndex + 1)
    }

    fun removeSelectedItems() {
        @Suppress("ControlFlowWithEmptyBody")
        while (deleteSelectedItem()) {
        }
    }

    private fun deleteSelectedItem(): Boolean {
        val selectedItemIndex = songItems.indexOfFirst { item -> item.isSelected.value!! }
        if (selectedItemIndex == -1) {
            return false
        }
        songItems.removeAt(selectedItemIndex)
        notifyItemRemoved(selectedItemIndex)
        return true
    }

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_song)
        private val handleView: View = itemView.findViewById(R.id.imv_handle)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)

        fun bind(position: Int) {
            val item = songItems[position]
            selectionTracker?.attach(this)
            setupListeners()

            item.isSelected.observe(context.getLifecycleOwner()!!) {
                checkBox.isChecked = it
            }

            showCheckBox
                .observe(context.getLifecycleOwner()!!, VisibilityObserver(handleView, true))
            showCheckBox.observe(context.getLifecycleOwner()!!, VisibilityObserver(checkBox))

            titleTextView.text = item.song.title
        }

        private fun setupListeners() {
            @SuppressLint("ClickableViewAccessibility")
            if (onHandleTouchListener != null) {
                handleView.setOnTouchListener { view, event ->
                    onHandleTouchListener.execute(this, view, event)
                }
            }
        }
    }
}