/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 21:22
 */

package pl.gunock.lyriccast.ui.shared.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ItemSongBinding
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class SongItemsAdapter(
    context: Context,
    private val selectionTracker: SelectionTracker<BaseViewHolder>?
) : ListAdapter<SongItem, SongItemsAdapter.ViewHolder>(DiffCallback()) {

    private companion object {
        const val TAG = "SongItemsAdapter"
    }

    private class DiffCallback : DiffUtil.ItemCallback<SongItem>() {
        override fun areItemsTheSame(oldItem: SongItem, newItem: SongItem): Boolean =
            oldItem.song.id == newItem.song.id

        override fun areContentsTheSame(oldItem: SongItem, newItem: SongItem): Boolean =
            oldItem == newItem
    }

    private val defaultItemCardColor = context.getColor(R.color.window_background_2)
    private val withCategoryTextColor = context.getColor(R.color.text_item_with_category)
    private val noCategoryTextColor = context.getColor(R.color.text_item_no_category)
    private val checkBoxColors = context.getColorStateList(R.color.checkbox_state_list)

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSongBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        if (currentList.isEmpty()) {
            return -1L
        }

        return currentList[position].song.idLong
    }

    inner class ViewHolder(
        private val binding: ItemSongBinding
    ) : BaseViewHolder(binding.root, selectionTracker) {
        init {
            binding.tvSongCategory.setTextColor(this@SongItemsAdapter.withCategoryTextColor)
        }

        override fun setupViewHolder(position: Int) {
            val item = try {
                currentList[position]
            } catch (e: IndexOutOfBoundsException) {
                Log.w(TAG, e)
                return
            }

            if (item.hasCheckbox) {
                binding.chkItemSong.visibility = View.VISIBLE
                binding.chkItemSong.isChecked = item.isSelected
            } else {
                binding.chkItemSong.visibility = View.GONE
            }

            binding.tvItemSongTitle.text = item.song.title

            if (item.song.category != null) {
                binding.tvSongCategory.text = item.song.category?.name

                binding.chkItemSong.buttonTintList =
                    ColorStateList.valueOf(this@SongItemsAdapter.withCategoryTextColor)

                binding.tvItemSongTitle.setTextColor(this@SongItemsAdapter.withCategoryTextColor)
                binding.root.setCardBackgroundColor(item.song.category?.color!!)
            } else {
                binding.tvSongCategory.text = ""

                binding.chkItemSong.buttonTintList = checkBoxColors

                binding.tvItemSongTitle.setTextColor(noCategoryTextColor)
                binding.root.setCardBackgroundColor(this@SongItemsAdapter.defaultItemCardColor)
            }
        }
    }
}