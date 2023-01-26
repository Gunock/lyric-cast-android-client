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
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.databinding.ItemSetlistSongBinding
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class SetlistSongItemsAdapter(
    context: Context,
    private val items: List<SetlistSongItem>,
    private val selectionTracker: SelectionTracker<BaseViewHolder>?,
    private val onHandleTouchListener: TouchAdapterItemListener<BaseViewHolder>? = null
) : RecyclerView.Adapter<SetlistSongItemsAdapter.ViewHolder>() {

    private companion object {
        const val TAG = "SetlistSongItemsAdapter"
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
        if (items.isEmpty()) {
            return -1L
        }

        return items[position].id
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: ItemSetlistSongBinding
    ) : BaseViewHolder(binding.root, selectionTracker) {

        override fun setupViewHolder(position: Int) {
            val item = try {
                items[position]
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
                binding.imvHandle.visibility = View.VISIBLE
            }

            binding.tvItemSongTitle.text = item.song.title
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