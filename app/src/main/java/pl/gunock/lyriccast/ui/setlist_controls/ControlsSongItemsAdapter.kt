/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 21:22
 */

package pl.gunock.lyriccast.ui.setlist_controls

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ItemControlsSongBinding
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.ui.shared.listeners.LongClickAdapterItemListener


class ControlsSongItemsAdapter(
    context: Context,
    val songItems: List<SongItem>,
    private val onItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    private val onItemClickListener: ClickAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<ControlsSongItemsAdapter.ViewHolder>() {

    private val highlightCardColor = context.getColor(R.color.accent)
    private val defaultCardColor = context.getColor(R.color.background_4)
    private val darkTextColor = context.getColor(R.color.dark_text)
    private val brightTextColor = context.getColor(R.color.bright_text)
    private val songItemTextColorMap = createCategoryTextColorMap()

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemControlsSongBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = songItems.size

    private fun createCategoryTextColorMap(): MutableMap<Int, Int> {
        val cardBackgroundColors = listOf(defaultCardColor, highlightCardColor)
        return cardBackgroundColors.associateWith(::getSongItemTextColor).toMutableMap()
    }

    private fun getSongItemTextColor(backgroundColor: Int): Int {
        val brightTextContrast =
            ColorUtils.calculateContrast(brightTextColor, backgroundColor)
        val darkTextContrast =
            ColorUtils.calculateContrast(darkTextColor, backgroundColor)

        return if (brightTextContrast > darkTextContrast) {
            brightTextColor
        } else {
            darkTextColor
        }
    }

    inner class ViewHolder(
        private val binding: ItemControlsSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val item: SongItem = songItems[position]
            val titleText = itemView.context.resources.getString(
                R.string.setlist_controls_song_item_title_template,
                absoluteAdapterPosition + 1,
                item.song.title
            )
            binding.tvItemSongTitle.text = titleText

            if (item.isHighlighted) {
                binding.tvItemSongTitle.setTextColor(songItemTextColorMap[highlightCardColor]!!)
                binding.root.setCardBackgroundColor(highlightCardColor)
            } else {
                binding.tvItemSongTitle.setTextColor(songItemTextColorMap[defaultCardColor]!!)
                binding.root.setCardBackgroundColor(defaultCardColor)
            }

            setupListeners()
        }

        private fun setupListeners() {
            if (onItemLongClickListener != null) {
                binding.root.setOnLongClickListener { view ->
                    onItemLongClickListener.execute(this, absoluteAdapterPosition, view)
                }
            }

            if (onItemClickListener != null) {
                binding.root.setOnClickListener { view ->
                    onItemClickListener.execute(this, absoluteAdapterPosition, view)
                }
            }
        }
    }
}