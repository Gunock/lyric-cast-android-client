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
import pl.gunock.lyriccast.models.ColorItem


class ColorSpinnerAdapter(
    context: Context,
    private val mColors: Array<ColorItem>
) : BaseAdapter() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItem(position: Int): Any {
        return mColors[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getCount(): Int {
        return mColors.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View =
            convertView ?: mInflater.inflate(R.layout.spinner_item_color, parent, false)

        val vh = ViewHolder(view)
        val item = mColors[position]
        vh.bind(item)

        return view
    }

    private inner class ViewHolder(itemView: View) {
        private val mNameTextView: TextView = itemView.findViewById(R.id.tv_spinner_color_name)
        private val mColorCardView: CardView =
            itemView.findViewById(R.id.cdv_spinner_category_color)

        fun bind(item: ColorItem) {
            mNameTextView.text = item.name
            mColorCardView.setCardBackgroundColor(item.value)
        }
    }

}