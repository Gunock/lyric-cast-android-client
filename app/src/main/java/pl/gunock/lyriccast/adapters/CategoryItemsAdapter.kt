/*
 * Created by Tomasz Kiljańczyk on 3/6/21 11:16 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/6/21 11:15 PM
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
import pl.gunock.lyriccast.models.CategoryItem

class CategoryItemsAdapter(
    var categoryItems: MutableList<CategoryItem>,
    var showCheckBox: Boolean = false,
    val onItemLongClickListener: LongClickAdapterItemListener<CategoryViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<CategoryViewHolder>? = null
) : RecyclerView.Adapter<CategoryItemsAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_category)
        private val name: TextView = itemView.findViewById(R.id.tv_category_name)
        private val categoryCard: CardView = itemView.findViewById(R.id.cdv_category_color)

        fun bind(item: CategoryItem) = with(itemView) {
            name.text = categoryItems[layoutPosition].name

            if (!showCheckBox) {
                checkBox.visibility = View.GONE
            } else {
                checkBox.visibility = View.VISIBLE
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    item.isSelected = isChecked
                }

                checkBox.isChecked = item.isSelected
            }

            categoryCard.setCardBackgroundColor(item.color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        return CategoryViewHolder(textView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categoryItems[position]

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

    override fun getItemCount() = categoryItems.size
}