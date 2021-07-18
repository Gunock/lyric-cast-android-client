/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:16
 */

package pl.gunock.lyriccast.ui.shared.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import io.realm.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.SpinnerItemColorBinding
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import java.util.*

class CategorySpinnerAdapter(
    context: Context
) : BaseAdapter() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mItems: MutableList<CategoryDocument> = mutableListOf()

    private val mCategoryAll: CategoryDocument =
        CategoryDocument(name = context.getString(R.string.category_all), id = ObjectId(Date(0), 0))

    val categories: List<CategoryDocument> get() = mItems

    suspend fun submitCollection(
        categories: RealmResults<CategoryDocument>,
        firstCategory: CategoryDocument = mCategoryAll
    ) {
        val frozenCategories = categories.freeze()
        withContext(Dispatchers.Default) {
            mItems.clear()
            mItems.addAll(listOf(firstCategory) + frozenCategories.toSortedSet())
        }
        notifyDataSetChanged()
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