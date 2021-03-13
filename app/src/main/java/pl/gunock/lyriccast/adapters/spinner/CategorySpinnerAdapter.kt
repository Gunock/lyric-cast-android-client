/*
 * Created by Tomasz Kiljańczyk on 3/13/21 3:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/13/21 3:21 PM
 */

package pl.gunock.lyriccast.adapters.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.models.Category

class CategorySpinnerAdapter(
    context: Context,
    private val categories: List<Category>
) : BaseAdapter() {

    constructor(context: Context, categories: Set<Category>) : this(
        context,
        categories.toList()
    )

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItem(position: Int): Any {
        return categories[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getCount(): Int {
        return categories.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.spinner_item_color, parent, false)

        val vh = ViewHolder(view)
        val item = categories[position]
        vh.bind(item)

        return view
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_spinner_color_name)
        private val colorCard: CardView = itemView.findViewById(R.id.cdv_spinner_category_color)

        fun bind(item: Category) {
            name.text = item.name
            if (item.color != null) {
                colorCard.setCardBackgroundColor(item.color)
            } else {
                colorCard.visibility = View.GONE
            }
        }
    }

}