/*
 * Created by Tomasz Kiljańczyk on 3/6/21 11:16 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/6/21 11:15 PM
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.models.ColorItem


class ColorSpinnerAdapter(
    context: Context,
    private val colors: Array<ColorItem>
) : BaseAdapter() {

    private inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_spinner_color_name)
        private val colorCard: CardView = itemView.findViewById(R.id.cdv_spinner_category_color)

        fun bind(item: ColorItem) {
            name.text = item.name
            colorCard.setCardBackgroundColor(item.value)
        }
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return colors.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        if (convertView == null) {
            view = inflater.inflate(R.layout.spinner_item_color, parent, false)
            val vh = CategoryViewHolder(view)
            val item = colors[position]
            vh.bind(item)
        } else {
            view = convertView
        }

        return view
    }

}