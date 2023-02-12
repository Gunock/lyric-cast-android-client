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
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ItemSongBinding
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.selection.SelectionViewHolder

class SongItemsAdapter(
    context: Context,
) : ListAdapter<SongItem, SongItemsAdapter.ViewHolder>(DiffCallback()) {

    private companion object {
        const val TAG = "SongItemsAdapter"
    }

    var onItemClickListener: ((SongItem?) -> Unit)? = null

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
        val item = try {
            currentList[position]
        } catch (e: IndexOutOfBoundsException) {
            Log.w(TAG, e)
            return
        }

        onItemClickListener?.let {
            holder.itemView.setOnClickListener { onItemClickListener?.invoke(holder.item) }
        }

        holder.bind(item)
    }

    override fun getItemId(position: Int): Long {
        if (currentList.isEmpty()) {
            return RecyclerView.NO_ID
        }

        return currentList[position].song.idLong
    }

    inner class ViewHolder(
        private val binding: ItemSongBinding
    ) : SelectionViewHolder<SongItem>(binding.root) {
        init {
            binding.tvSongCategory.setTextColor(withCategoryTextColor)
        }

        override fun bindAction(item: SongItem) {
            if (item.hasCheckbox) {
                binding.chkItemSong.visibility = View.VISIBLE
                binding.chkItemSong.isChecked = item.isSelected
            } else {
                binding.chkItemSong.visibility = View.GONE
                binding.chkItemSong.isChecked = false
            }

            if (item.song != this.item?.song) {
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

    private class DiffCallback : DiffUtil.ItemCallback<SongItem>() {
        override fun areItemsTheSame(oldItem: SongItem, newItem: SongItem): Boolean =
            oldItem.song.id == newItem.song.id

        override fun areContentsTheSame(oldItem: SongItem, newItem: SongItem): Boolean =
            oldItem == newItem
    }
}