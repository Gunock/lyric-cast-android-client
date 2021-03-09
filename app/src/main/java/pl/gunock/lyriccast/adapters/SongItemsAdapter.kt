/*
 * Created by Tomasz Kiljańczyk on 3/8/21 11:19 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/8/21 11:08 PM
 */

package pl.gunock.lyriccast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SongItem

class SongItemsAdapter(
    var songItems: MutableList<SongItem>,
    var showCheckBox: Boolean = false,
    val showRowNumber: Boolean = false,
    val showAuthor: Boolean = true,
    val onItemLongClickListener: LongClickAdapterItemListener<SongViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<SongViewHolder>? = null
) : RecyclerView.Adapter<SongItemsAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val item = songItems[position]

        if (onItemLongClickListener != null) {
            holder.itemCardView.setOnLongClickListener { view ->
                onItemLongClickListener.execute(holder, position, view)
            }
        }

        if (onItemClickListener != null) {
            holder.itemCardView.setOnClickListener { view ->
                onItemClickListener.execute(holder, position, view)
            }
        }

        holder.bind(item)
    }

    override fun getItemCount() = songItems.size

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_song)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)
        private val authorTextView: TextView = itemView.findViewById(R.id.tv_item_song_author)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tv_song_category)
        private val categoryCardView: CardView = itemView.findViewById(R.id.cdv_category_color)
        val itemCardView: CardView = itemView.findViewById(R.id.item_song)

        fun bind(item: SongItem) {
            if (!showRowNumber) {
                titleTextView.text = item.title
            } else {
                val titleText = itemView.context.resources.getString(
                    R.string.item_song_item_title_template,
                    layoutPosition + 1,
                    item.title
                )
                titleTextView.text = titleText
            }
            authorTextView.text = item.author

            if (item.category != null) {
                categoryTextView.text = item.category.name
                categoryCardView.setCardBackgroundColor(item.category.color!!)
            } else {
                itemView.findViewById<CardView>(R.id.cdv_category_color).visibility = View.INVISIBLE
            }

            if (!showAuthor) {
                authorTextView.visibility = View.GONE
            }

            if (!showCheckBox) {
                checkBox.visibility = View.GONE
            } else {
                checkBox.visibility = View.VISIBLE
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    item.isSelected = isChecked
                }

                checkBox.isChecked = item.isSelected
            }
        }
    }

}