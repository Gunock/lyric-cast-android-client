/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 23:08
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

class CategorySpinnerAdapter(
    context: Context
) : BaseAdapter() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mItems: MutableList<Category> = mutableListOf()

    private val mCategoryAll: Category =
        Category(name = context.getString(R.string.category_all), id = "")

    val categories: List<Category> get() = mItems

    suspend fun submitCollection(
        categories: Iterable<Category>,
        firstCategory: Category = mCategoryAll
    ) {
        withContext(Dispatchers.Default) {
            mItems.clear()
            mItems.addAll(listOf(firstCategory) + categories.toSortedSet())
        }
        withContext(Dispatchers.Main) {
            notifyDataSetChanged()
        }
    }

    override fun getItem(position: Int): Any {
        return this.categories[position]
    }

    override fun getItemId(position: Int): Long {
        return this.categories[position].idLong
    }

    override fun getCount(): Int {
        return this.categories.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView != null) {
            SpinnerItemColorBinding.bind(convertView)
        } else {
            SpinnerItemColorBinding.inflate(mInflater)
        }

        val viewHolder = ViewHolder(binding)
        val item = this.categories[position]
        viewHolder.bind(item)

        return binding.root
    }

    private inner class ViewHolder(private val mBinding: SpinnerItemColorBinding) {
        fun bind(item: Category) {
            mBinding.tvSpinnerColorName.text = item.name
            if (item.color != null) {
                mBinding.cdvSpinnerCategoryColor.visibility = View.VISIBLE
                mBinding.cdvSpinnerCategoryColor.setCardBackgroundColor(item.color!!)
            } else {
                mBinding.cdvSpinnerCategoryColor.visibility = View.GONE
            }
        }
    }

}