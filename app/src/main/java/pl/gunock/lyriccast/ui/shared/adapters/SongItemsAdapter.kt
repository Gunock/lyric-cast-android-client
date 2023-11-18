/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 21:22
 */

package pl.gunock.lyriccast.ui.shared.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
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

    private val brightCategoryTextColor = context.getColor(R.color.bright_text)
    private val darkCategoryTextColor = context.getColor(R.color.dark_text)
    private val categoryTextColorMap = createCategoryTextColorMap(context)

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

    private fun createCategoryTextColorMap(context: Context): MutableMap<Int, Int> {
        val categoryColorValues = context.resources.getIntArray(R.array.category_color_values)
        return categoryColorValues.associateWith(::getCategoryTextColor).toMutableMap()
    }

    private fun getCategoryTextColor(backgroundColor: Int): Int {
        val brightTextContrast =
            ColorUtils.calculateContrast(brightCategoryTextColor, backgroundColor)
        val darkTextContrast =
            ColorUtils.calculateContrast(darkCategoryTextColor, backgroundColor)

        return if (brightTextContrast > darkTextContrast) {
            brightCategoryTextColor
        } else {
            darkCategoryTextColor
        }
    }

    inner class ViewHolder(
        private val binding: ItemSongBinding
    ) : SelectionViewHolder<SongItem>(binding.root) {
        override fun bindAction(item: SongItem) {
            if (item.hasCheckbox) {
                binding.chkItemSong.visibility = View.VISIBLE
                binding.chkItemSong.isChecked = item.isSelected
            } else {
                binding.chkItemSong.visibility = View.GONE
                binding.chkItemSong.isChecked = false
            }

            if (item.song == this.item?.song) {
                return
            }

            binding.tvItemSongTitle.text = item.song.title

            if (item.song.category == null || item.song.category!!.color == 0) {
                binding.tvSongCategory.text = ""
                binding.cardSongCategory.visibility = View.INVISIBLE
            } else {
                if (item.song.category!!.color != null) {
                    val backgroundColor = item.song.category!!.color!!
                    var categoryTextColor = categoryTextColorMap[backgroundColor]
                    if (categoryTextColor == null) {
                        categoryTextColor = getCategoryTextColor(backgroundColor)
                        categoryTextColorMap[backgroundColor] = categoryTextColor
                    }

                    binding.cardSongCategory.setCardBackgroundColor(backgroundColor)
                    binding.tvSongCategory.setTextColor(categoryTextColor)
                }

                binding.tvSongCategory.text = item.song.category!!.name
                binding.cardSongCategory.visibility = View.VISIBLE
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