/*
 * Created by Tomasz Kiljańczyk on 3/12/21 4:03 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/12/21 4:03 PM
 */

package pl.gunock.lyriccast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
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

    inner class SetlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_setlist)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_item_setlist_name)

        fun bind(item: SetlistItem) {
            nameTextView.text = setlistItems[layoutPosition].name

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