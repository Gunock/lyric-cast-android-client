/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:55 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:36 PM
 */

package pl.gunock.lyriccast.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SetlistItem

class SetlistItemsAdapter(
    var setlistItems: MutableList<SetlistItem>,
    var showCheckBox: Boolean = false,
    val onItemLongClickListener: LongClickAdapterItemListener<SetlistViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<SetlistViewHolder>? = null
) : RecyclerView.Adapter<SetlistItemsAdapter.SetlistViewHolder>() {

    inner class SetlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_setlist)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_setlist_name)
        private val categoryCardView: CardView =
            itemView.findViewById(R.id.cdv_item_setlist_category)
        private val itemLayout: LinearLayout = itemView.findViewById(R.id.item_setlist)

        fun bind(item: SetlistItem) = with(itemView) {
            titleTextView.text = setlistItems[layoutPosition].name

            if (item.category.isBlank()) {
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

            if (isSelected) {
                itemLayout.setBackgroundColor(
                    itemView.resources.getColor(
                        R.color.colorAccent,
                        null
                    )
                )
            } else {
                itemLayout.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetlistViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setlist, parent, false)

        return SetlistViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SetlistViewHolder, position: Int) {
        val item = setlistItems[position]

        if (onItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener { view ->
                onItemLongClickListener.execute(holder, position, view)
            }
        }

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener { view ->
                onItemClickListener.execute(holder, position, view)
            }
        }

        holder.bind(item)
    }

    override fun getItemCount() = setlistItems.size
}