/*
 * Created by Tomasz Kiljańczyk on 3/15/21 1:22 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 1:20 AM
 */

package pl.gunock.lyriccast.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.extensions.getLifecycleOwner
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.models.SongItem

class SetlistSongItemsAdapter(
    val context: Context,
    var songItems: MutableList<SongItem>,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val showHandle: MutableLiveData<Boolean> = MutableLiveData(true),
    val onItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<ViewHolder>? = null,
    val onHandleTouchListener: TouchAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<SetlistSongItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setlist_song, parent, false)

        showCheckBox.observe(context.getLifecycleOwner()!!) {
            if (!it) {
                songItems.forEach { item -> item.isSelected = false }
            }
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = songItems[position]

        @SuppressLint("ClickableViewAccessibility")
        if (onHandleTouchListener != null) {
            holder.handleView.setOnTouchListener { view, event ->
                onHandleTouchListener.execute(holder, view, event)
            }
        }

        holder.bind(item)
    }

    override fun getItemCount() = songItems.size

    fun moveItem(from: Int, to: Int) {
        val item = songItems.removeAt(from)
        songItems.add(to, item)
    }

    fun duplicateSelectedItem() {
        val selectedItemIndex = songItems.indexOfFirst { item -> item.isSelected }
        val selectedItem = songItems[selectedItemIndex]
        selectedItem.isSelected = false

        songItems.add(selectedItemIndex + 1, selectedItem)
        notifyItemInserted(selectedItemIndex + 1)
    }

    fun removeSelectedItems() {
        @Suppress("ControlFlowWithEmptyBody")
        while (deleteSelectedItem()) {
        }
    }

    private fun deleteSelectedItem(): Boolean {
        val selectedItemIndex = songItems.indexOfFirst { item -> item.isSelected }
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
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_song)
        val handleView: View = itemView.findViewById(R.id.imv_handle)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)
        private val itemCardView: CardView = itemView.findViewById(R.id.item_song)

        fun bind(item: SongItem) {
            titleTextView.text = item.title

            setupListeners()

            showCheckBox.observe(context.getLifecycleOwner()!!, this::observeShowCheckbox)
            showHandle.observe(context.getLifecycleOwner()!!, this::observeShowHandle)
        }

        private fun observeShowCheckbox(value: Boolean) {
            if (value) {
                checkBox.visibility = View.VISIBLE
            } else {
                checkBox.visibility = View.GONE
            }
        }

        private fun observeShowHandle(value: Boolean) {
            if (value) {
                handleView.visibility = View.VISIBLE
            } else {
                handleView.visibility = View.GONE
            }
        }

        private fun setupListeners() {
            if (onItemLongClickListener != null) {
                itemCardView.setOnLongClickListener { view ->
                    onItemLongClickListener.execute(this, adapterPosition, view)
                }
            }

            if (onItemClickListener != null) {
                itemCardView.setOnClickListener { view ->
                    onItemClickListener.execute(this, adapterPosition, view)
                }
            }
        }
    }
}