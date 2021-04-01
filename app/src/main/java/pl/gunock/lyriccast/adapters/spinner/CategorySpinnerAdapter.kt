/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/31/21 6:21 PM
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
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var _items: MutableList<Category> = mutableListOf()
    val categories: List<Category> get() = _items

    fun submitCollection(categories: Collection<Category>) {
        _items.clear()
        _items.addAll(listOf(Category.ALL_CATEGORY) + categories.toSortedSet())
        notifyDataSetChanged()
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
        val view: View = convertView ?: inflater.inflate(R.layout.spinner_item_color, parent, false)

        val vh = ViewHolder(view)
        val item = this.categories[position]
        vh.bind(item)

        return view
    }

    private inner class ViewHolder(itemView: View) {
        private val name: TextView = itemView.findViewById(R.id.tv_spinner_color_name)
        private val colorCard: CardView = itemView.findViewById(R.id.cdv_spinner_category_color)

        fun bind(item: Category) {
            name.text = item.name
            if (item.color != null) {
                colorCard.visibility = View.VISIBLE
                colorCard.setCardBackgroundColor(item.color!!)
            } else {
                colorCard.visibility = View.GONE
            }
        }
    }

}