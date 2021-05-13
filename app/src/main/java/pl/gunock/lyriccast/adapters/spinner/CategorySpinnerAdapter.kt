/*
 * Created by Tomasz Kiljanczyk on 14/05/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 13/05/2021, 23:42
 */

package pl.gunock.lyriccast.adapters.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.bson.types.ObjectId
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.SpinnerItemColorBinding
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import java.util.*

class CategorySpinnerAdapter(
    context: Context
) : BaseAdapter() {
    private val mLock = Any()

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mItems: MutableList<CategoryDocument> = mutableListOf()

    private val mCategoryAll: CategoryDocument =
        CategoryDocument(name = context.getString(R.string.category_all), id = ObjectId(Date(0), 0))

    val categories: List<CategoryDocument> get() = mItems

    fun submitCollection(
        categories: Collection<CategoryDocument>,
        firstCategory: CategoryDocument = mCategoryAll
    ) {
        synchronized(mLock) {
            mItems.clear()
            mItems.addAll(listOf(firstCategory) + categories.toSortedSet())
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
        val view: View =
            convertView ?: mInflater.inflate(R.layout.spinner_item_color, parent, false)

        val vh = ViewHolder(view)
        val item = this.categories[position]
        vh.bind(item)

        return view
    }

    private inner class ViewHolder(itemView: View) {
        private val mBinding = SpinnerItemColorBinding.bind(itemView)

        fun bind(item: CategoryDocument) {
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