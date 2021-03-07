/*
 * Created by Tomasz Kiljańczyk on 3/7/21 11:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/7/21 10:43 PM
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
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
    val context: Context,
    var categoryItems: MutableList<CategoryItem>,
    var showCheckBox: Boolean = false,
    val onItemLongClickListener: LongClickAdapterItemListener<CategoryViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<CategoryViewHolder>? = null
) : RecyclerView.Adapter<CategoryItemsAdapter.CategoryViewHolder>() {

    private val colorMap: Map<String, Int>

    init {
        val colorNames = context.resources.getStringArray(R.array.category_color_names)
        val colorValues = context.resources.getIntArray(R.array.category_color_values)

        colorMap = colorNames.zip<String, Int>(colorValues.toList()).toMap()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        return CategoryViewHolder(textView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = categoryItems[position]
        holder.bind(item)

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
    }

    override fun getItemCount() = categoryItems.size

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

            val color = colorMap[item.color]!!
            categoryCard.setCardBackgroundColor(color)
        }
    }

}