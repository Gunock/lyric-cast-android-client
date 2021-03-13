/*
 * Created by Tomasz Kiljańczyk on 3/13/21 3:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/13/21 2:57 PM
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
    val onItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<SongItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = songItems[position]

        holder.bind(item)
    }

    override fun getItemCount() = songItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_song)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tv_song_category)
        private val categoryCardView: CardView = itemView.findViewById(R.id.cdv_category_color)
        private val itemCardView: CardView = itemView.findViewById(R.id.item_song)

        fun bind(item: SongItem) {
            setupListeners()

            titleTextView.text = item.title

            if (item.category != null) {
                categoryTextView.text = item.category.name
                categoryCardView.setCardBackgroundColor(item.category.color!!)
            } else {
                categoryCardView.visibility = View.INVISIBLE
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

        fun setupListeners() {
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