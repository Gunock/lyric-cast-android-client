/*
 * Created by Tomasz Kiljańczyk on 3/15/21 1:45 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 1:40 AM
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.extensions.getLifecycleOwner
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.misc.VisibilityObserver
import pl.gunock.lyriccast.models.SongItem

class SongItemsAdapter(
    val context: Context,
    var songItems: MutableList<SongItem>,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val onItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<SongItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = songItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_song)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tv_song_category)
        private val categoryCardView: CardView = itemView.findViewById(R.id.cdv_category_color)
        private val itemCardView: CardView = itemView.findViewById(R.id.item_song)

        fun bind() {
            setupListeners()
            showCheckBox.observe(context.getLifecycleOwner()!!, VisibilityObserver(checkBox))

            val item = songItems[adapterPosition]
            titleTextView.text = item.title

            if (item.category != null) {
                categoryTextView.text = item.category.name
                categoryCardView.setCardBackgroundColor(item.category.color!!)
            } else {
                categoryCardView.visibility = View.INVISIBLE
            }

            checkBox.isChecked = item.isSelected
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