/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 19:57
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 09/03/2022, 08:59
 */

package pl.gunock.lyriccast.ui.shared.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ItemSongBinding
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class SongItemsAdapter(
    context: Context,
    private val selectionTracker: SelectionTracker<BaseViewHolder>?
) : RecyclerView.Adapter<SongItemsAdapter.ViewHolder>() {

    private companion object {
        const val TAG = "SongItemsAdapter"
    }

    init {
        setHasStableIds(true)
    }

    private val defaultItemCardColor = context.getColor(R.color.window_background_2)
    private val withCategoryTextColor = context.getColor(R.color.text_item_with_category)
    private val noCategoryTextColor = context.getColor(R.color.text_item_no_category)
    private val checkBoxColors = context.getColorStateList(R.color.checkbox_state_list)

    private var _items: List<SongItem> = listOf()

    fun submitCollection(songs: List<SongItem>) {
        val previousSize = itemCount
        _items = songs
        notifyItemRangeRemoved(0, previousSize)
        notifyItemRangeRemoved(0, _items.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        if (_items.isEmpty()) {
            return -1L
        }

        return _items[position].song.idLong
    }

    override fun getItemCount() = _items.size

    inner class ViewHolder(
        private val binding: ItemSongBinding
    ) : BaseViewHolder(binding.root, selectionTracker) {
        init {
            binding.tvSongCategory.setTextColor(this@SongItemsAdapter.withCategoryTextColor)
        }

        override fun setUpViewHolder(position: Int) {
            val item = try {
                _items[position]
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