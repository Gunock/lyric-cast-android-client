/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 19:46
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 19:46
 */

package pl.gunock.lyriccast.ui.shared.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.SpinnerItemColorBinding
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.domain.models.CategoryItem

class CategorySpinnerAdapter(
    context: Context
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val categoryAll: CategoryItem =
        CategoryItem(Category(name = context.getString(R.string.category_all), id = ""))

    val items: List<CategoryItem> get() = _items

    private var _items: MutableList<CategoryItem> = mutableListOf()

    suspend fun submitCollection(
        categories: List<CategoryItem>,
        firstCategory: CategoryItem = categoryAll
    ) {
        withContext(Dispatchers.Default) {
            _items.clear()
            _items.addAll(listOf(firstCategory) + categories)
        }
        withContext(Dispatchers.Main) {
            notifyDataSetChanged()
        }
    }

    override fun getItem(position: Int): Any {
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
            SpinnerItemColorBinding.bind(convertView)
        } else {
            SpinnerItemColorBinding.inflate(inflater)
        }

        val viewHolder = ViewHolder(binding)
        val item = _items[position]
        viewHolder.bind(item.category)

        return binding.root
    }

    private inner class ViewHolder(private val binding: SpinnerItemColorBinding) {
        fun bind(category: Category) {
            binding.tvSpinnerColorName.text = category.name
            if (category.color != null) {
                binding.cdvSpinnerCategoryColor.visibility = View.VISIBLE
                binding.cdvSpinnerCategoryColor.setCardBackgroundColor(category.color!!)
            } else {
                binding.cdvSpinnerCategoryColor.visibility = View.GONE
            }
        }
    }

}