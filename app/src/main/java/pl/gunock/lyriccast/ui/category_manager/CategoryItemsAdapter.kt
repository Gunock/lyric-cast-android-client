/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 21:22
 */

package pl.gunock.lyriccast.ui.category_manager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.databinding.ItemCategoryBinding
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class CategoryItemsAdapter(
    context: Context,
    private val selectionTracker: SelectionTracker<BaseViewHolder>?
) : RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder>() {

    private companion object {
        const val TAG = "CategoryItemsAdapter"
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var _items: List<CategoryItem> = listOf()

    init {
        setHasStableIds(true)
    }

    fun submitCollection(categories: List<CategoryItem>) {
        val previousSize = itemCount
        _items = categories
        notifyItemRangeRemoved(0, previousSize)
        notifyItemRangeInserted(0, _items.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        if (_items.isEmpty()) {
            return -1L
        }

        return _items[position].category.idLong
    }

    override fun getItemCount() = _items.size

    inner class ViewHolder(
        private val binding: ItemCategoryBinding
    ) : BaseViewHolder(binding.root, selectionTracker) {
        override fun setupViewHolder(position: Int) {
            val item: CategoryItem = try {
                _items[position]
            } catch (e: IndexOutOfBoundsException) {
                Log.w(TAG, e)
                return
            }

            if (item.hasCheckbox) {
                binding.chkItemCategory.visibility = View.VISIBLE
                binding.chkItemCategory.isChecked = item.isSelected
            } else {
                binding.chkItemCategory.visibility = View.GONE
            }

            binding.tvCategoryName.text = _items[absoluteAdapterPosition].category.name

            if (item.category.color != null) {
                binding.cdvCategoryColor.setCardBackgroundColor(item.category.color!!)
            }
        }
    }
}