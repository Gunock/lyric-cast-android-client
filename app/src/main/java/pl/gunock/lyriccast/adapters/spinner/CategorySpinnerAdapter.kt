/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:14 PM
 */

package pl.gunock.lyriccast.adapters.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.entities.Category

class CategorySpinnerAdapter(
    context: Context
) : BaseAdapter() {
    private val mLock = Any()
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mItems: MutableList<Category> = mutableListOf()
    val categories: List<Category> get() = mItems

    fun submitCollection(categories: Collection<Category>, firstCategory: Category = Category.ALL) {
        synchronized(mLock) {
            mItems.clear()
            mItems.addAll(listOf(firstCategory) + categories.toSortedSet())
            notifyDataSetChanged()
        }
    }

    override fun getItem(position: Int): Any {
        return this.categories[position]
    }

    override fun getItemId(position: Int): Long {
        return this.categories[position].id
    }

    override fun getCount(): Int {
        return this.categories.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View =
            convertView ?: mInflater.inflate(R.layout.spinner_item_color, parent, false)

        val vh = ViewHolder(view)
        val item = this.categories[position]
        vh.bind(item)

        return view
    }

    private inner class ViewHolder(itemView: View) {
        private val mNameTextView: TextView = itemView.findViewById(R.id.tv_spinner_color_name)
        private val mColorCardView: CardView =
            itemView.findViewById(R.id.cdv_spinner_category_color)

        fun bind(item: Category) {
            mNameTextView.text = item.name
            if (item.color != null) {
                mColorCardView.visibility = View.VISIBLE
                mColorCardView.setCardBackgroundColor(item.color!!)
            } else {
                mColorCardView.visibility = View.GONE
            }
        }
    }

}