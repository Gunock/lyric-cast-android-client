/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 21:22
 */

package pl.gunock.lyriccast.ui.setlist_editor.setlist

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import pl.gunock.lyriccast.databinding.ItemSetlistSongBinding
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class SetlistSongItemsAdapter(
    context: Context,
    private val selectionTracker: SelectionTracker<BaseViewHolder>?,
    private val onHandleTouchListener: TouchAdapterItemListener<BaseViewHolder>? = null
) : ListAdapter<SetlistSongItem, SetlistSongItemsAdapter.ViewHolder>(DiffCallback()) {

    private companion object {
        const val TAG = "SetlistSongItemsAdapter"
    }

    private class DiffCallback : DiffUtil.ItemCallback<SetlistSongItem>() {
        override fun areItemsTheSame(oldItem: SetlistSongItem, newItem: SetlistSongItem): Boolean =
            oldItem.song.id == newItem.song.id

        override fun areContentsTheSame(
            oldItem: SetlistSongItem,
            newItem: SetlistSongItem
        ): Boolean =
            oldItem == newItem
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetlistSongBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        if (currentList.isEmpty()) {
            return -1L
        }

        return currentList[position].id
    }

    override fun getItemCount() = currentList.size

    inner class ViewHolder(
        private val binding: ItemSetlistSongBinding
    ) : BaseViewHolder(binding.root, selectionTracker) {

        private var oldItem: SetlistSongItem? = null

        override fun setupViewHolder(position: Int) {
            val item = try {
                currentList[position]
            } catch (e: IndexOutOfBoundsException) {
                Log.w(TAG, e)
                return
            }

            setupListeners()

            if (item.hasCheckbox) {
                binding.chkItemSong.visibility = View.VISIBLE
                binding.chkItemSong.isChecked = item.isSelected
                binding.imvHandle.visibility = View.GONE
            } else {
                binding.chkItemSong.visibility = View.GONE
                binding.chkItemSong.isChecked = false
                binding.imvHandle.visibility = View.VISIBLE
            }

            if (item.song != oldItem?.song) {
                binding.tvItemSongTitle.text = item.song.title
            }

            oldItem = item
        }

        private fun setupListeners() {
            @SuppressLint("ClickableViewAccessibility")
            if (onHandleTouchListener != null) {
                binding.imvHandle.setOnTouchListener { view, event ->
                    onHandleTouchListener.execute(this, view, event)
                }
            }
        }
    }
}