/*
 * Created by Tomasz Kiljańczyk on 3/15/21 1:45 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 1:33 AM
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
import pl.gunock.lyriccast.models.CategoryItem

class CategoryItemsAdapter(
    val context: Context,
    var categoryItems: MutableList<CategoryItem>,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val onItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categoryItems[position]
        holder.bind(item)
    }

    override fun getItemCount() = categoryItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_category)
        private val name: TextView = itemView.findViewById(R.id.tv_category_name)
        private val colorCard: CardView = itemView.findViewById(R.id.cdv_category_color)

        fun bind(item: CategoryItem) {
            setupListeners()
            showCheckBox.observe(context.getLifecycleOwner()!!, VisibilityObserver(checkBox))

            name.text = categoryItems[adapterPosition].name

            if (!showCheckBox.value!!) {
                checkBox.visibility = View.GONE
            } else {
                checkBox.visibility = View.VISIBLE
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    item.isSelected = isChecked
                }

                checkBox.isChecked = item.isSelected
            }

            if (item.color != null) {
                colorCard.setCardBackgroundColor(item.color)
            }

        }

        private fun setupListeners() {
            if (onItemLongClickListener != null) {
                itemView.setOnLongClickListener { view ->
                    onItemLongClickListener.execute(this, adapterPosition, view)
                }
            }

            if (onItemClickListener != null) {
                itemView.setOnClickListener { view ->
                    onItemClickListener.execute(this, adapterPosition, view)
                }
            }
        }

    }

}