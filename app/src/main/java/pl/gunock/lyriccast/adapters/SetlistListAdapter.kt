/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:42 PM
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
import pl.gunock.lyriccast.adapters.listeners.ClickAdapterListener
import pl.gunock.lyriccast.adapters.listeners.LongClickAdapterListener
import pl.gunock.lyriccast.models.SetlistItemModel

class SetlistListAdapter(
    var setlistItems: MutableList<SetlistItemModel>,
    var showCheckBox: Boolean = false,
    val onLongClickListener: LongClickAdapterListener<SetlistViewHolder>? = null,
    val onClickListener: ClickAdapterListener<SetlistViewHolder>? = null
) : RecyclerView.Adapter<SetlistListAdapter.SetlistViewHolder>() {

    inner class SetlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.setlist_checkbox)
        private val titleTextView: TextView = itemView.findViewById(R.id.setlist_name)
        private val categoryCardView: CardView = itemView.findViewById(R.id.card_setlist_category)
        private val itemLayout: LinearLayout = itemView.findViewById(R.id.item_setlist)

        fun bind(item: SetlistItemModel) = with(itemView) {
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

        if (onLongClickListener != null) {
            holder.itemView.setOnLongClickListener { view ->
                onLongClickListener.execute(holder, position, view)
            }
        }

        if (onClickListener != null) {
            holder.itemView.setOnClickListener { view ->
                onClickListener.execute(holder, position, view)
            }
        }

        holder.bind(item)
    }

    override fun getItemCount() = setlistItems.size
}