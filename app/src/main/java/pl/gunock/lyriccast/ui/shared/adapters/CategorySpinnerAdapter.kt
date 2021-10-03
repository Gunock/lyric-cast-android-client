/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 12:04
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 11:54
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
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    private val mCategoryAll: CategoryItem =
        CategoryItem(Category(name = context.getString(R.string.category_all), id = ""))

    val items: List<CategoryItem> get() = _items

    private var _items: MutableList<CategoryItem> = mutableListOf()

    suspend fun submitCollection(
        categories: List<CategoryItem>,
        firstCategory: CategoryItem = mCategoryAll
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
        return this.items[position]
    }

    override fun getItemId(position: Int): Long {
        return this.items[position].category.idLong
    }

    override fun getCount(): Int {
        return this.items.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView != null) {
            SpinnerItemColorBinding.bind(convertView)
        } else {
            SpinnerItemColorBinding.inflate(mInflater)
        }

        val viewHolder = ViewHolder(binding)
        val item = this.items[position]
        viewHolder.bind(item.category)

        return binding.root
    }

    private inner class ViewHolder(private val mBinding: SpinnerItemColorBinding) {
        fun bind(category: Category) {
            mBinding.tvSpinnerColorName.text = category.name
            if (category.color != null) {
                mBinding.cdvSpinnerCategoryColor.visibility = View.VISIBLE
                mBinding.cdvSpinnerCategoryColor.setCardBackgroundColor(category.color!!)
            } else {
                mBinding.cdvSpinnerCategoryColor.visibility = View.GONE
            }
        }
    }

}