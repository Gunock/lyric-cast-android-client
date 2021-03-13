/*
 * Created by Tomasz Kiljańczyk on 3/13/21 3:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/13/21 3:05 PM
 */

package pl.gunock.lyriccast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SongItem

class ControlsSongItemsAdapter(
    var songItems: MutableList<SongItem>,
    val onItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<ControlsSongItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_controls_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = songItems[position]

        holder.bind(item)
    }

    override fun getItemCount() = songItems.size

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)
        private val itemCardView: CardView = itemView.findViewById(R.id.item_song)

        fun bind(item: SongItem) {
            val titleText = itemView.context.resources.getString(
                R.string.item_song_item_title_template,
                adapterPosition + 1,
                item.title
            )
            titleTextView.text = titleText

            setupListeners()
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