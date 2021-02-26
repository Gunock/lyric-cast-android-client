/*
 * Created by Tomasz Kiljańczyk on 2/26/21 9:36 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/26/21 9:36 PM
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
    var setlists: MutableList<SetlistItemModel>,
    var showCheckBox: Boolean = false,
    val onLongClickListener: LongClickAdapterListener<SetlistViewHolder>? = null,
    val onClickListener: ClickAdapterListener<SetlistViewHolder>? = null
) : RecyclerView.Adapter<SetlistListAdapter.SetlistViewHolder>() {

    inner class SetlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLayout: LinearLayout = itemView.findViewById(R.id.item_setlist)
        val titleTextView: TextView = itemView.findViewById(R.id.setlist_name)
        val categoryCardView: CardView = itemView.findViewById(R.id.card_setlist_category)
        val checkBox: CheckBox = itemView.findViewById(R.id.setlist_checkbox)

        fun bind(item: SetlistItemModel) = with(itemView) {
            titleTextView.text = setlists[layoutPosition].name

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
        val item = setlists[position]

        if (onLongClickListener != null) {
            holder.itemView.setOnLongClickListener {
                onLongClickListener.execute(holder, position, it)
            }
        }

        if (onClickListener != null) {
            holder.itemView.setOnClickListener {
                onClickListener.execute(holder, position, it)
            }
        }

        holder.bind(item)
    }

    override fun getItemCount() = setlists.size
}