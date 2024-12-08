/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.DropdownItemColorBinding
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.domain.models.CategoryItem

class CategorySpinnerAdapter(
    context: Context
) : ArrayAdapter<CategoryItem>(context, R.layout.dropdown_item_color) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val categoryAll: CategoryItem =
        CategoryItem(Category(name = context.getString(R.string.category_all), id = ""))

    private var _items: List<CategoryItem> = listOf()

    fun submitCollection(
        categories: List<CategoryItem>,
        firstCategory: CategoryItem = categoryAll
    ) {
        _items = listOf(firstCategory) + categories
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): CategoryItem {
        return _items[position]
    }

    override fun getItemId(position: Int): Long {
        if (_items.isEmpty()) {
            return -1L
        }

        return _items[position].category.idLong
    }

    override fun getCount(): Int = _items.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView != null) {
            DropdownItemColorBinding.bind(convertView)
        } else {
            DropdownItemColorBinding.inflate(inflater)
        }

        val viewHolder = ViewHolder(binding)
        val item = _items[position]
        viewHolder.bind(item.category)

        return binding.root
    }

    private inner class ViewHolder(private val binding: DropdownItemColorBinding) {
        fun bind(category: Category) {
            binding.textColorName.text = category.name
            if (category.color != null) {
                binding.cardCategoryColor.visibility = View.VISIBLE
                binding.cardCategoryColor.setCardBackgroundColor(category.color!!)
            } else {
                binding.cardCategoryColor.visibility = View.GONE
            }
        }
    }

}