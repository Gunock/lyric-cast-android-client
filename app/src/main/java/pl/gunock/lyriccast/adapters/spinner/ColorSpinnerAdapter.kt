/*
 * Created by Tomasz Kiljanczyk on 15/05/2021, 15:20
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 15/05/2021, 12:18
 */

package pl.gunock.lyriccast.adapters.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import pl.gunock.lyriccast.databinding.SpinnerItemColorBinding
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
        val binding = if (convertView != null) {
            SpinnerItemColorBinding.bind(convertView)
        } else {
            SpinnerItemColorBinding.inflate(mInflater)
        }

        val item = mColors[position]
        val viewHolder = ViewHolder(binding)
        viewHolder.bind(item)

        return binding.root
    }

    private inner class ViewHolder(private val mBinding: SpinnerItemColorBinding) {
        fun bind(item: ColorItem) {
            mBinding.tvSpinnerColorName.text = item.name
            mBinding.cdvSpinnerCategoryColor.setCardBackgroundColor(item.value)
        }
    }

}