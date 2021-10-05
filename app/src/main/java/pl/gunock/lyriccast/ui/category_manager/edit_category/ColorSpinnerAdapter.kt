/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 10:03
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 09:41
 */

package pl.gunock.lyriccast.ui.category_manager.edit_category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import pl.gunock.lyriccast.databinding.SpinnerItemColorBinding
import pl.gunock.lyriccast.domain.models.ColorItem


class ColorSpinnerAdapter(
    context: Context,
    private val colors: Array<ColorItem>
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItem(position: Int): Any {
        return colors[position]
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getCount(): Int {
        return colors.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView != null) {
            SpinnerItemColorBinding.bind(convertView)
        } else {
            SpinnerItemColorBinding.inflate(inflater)
        }

        val item = colors[position]
        val viewHolder = ViewHolder(binding)
        viewHolder.bind(item)

        return binding.root
    }

    private inner class ViewHolder(private val binding: SpinnerItemColorBinding) {
        fun bind(item: ColorItem) {
            binding.tvSpinnerColorName.text = item.name
            binding.cdvSpinnerCategoryColor.setCardBackgroundColor(item.value)
        }
    }

}