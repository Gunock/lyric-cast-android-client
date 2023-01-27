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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import pl.gunock.lyriccast.databinding.ItemCategoryBinding
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class CategoryItemsAdapter(
    context: Context,
    private val selectionTracker: SelectionTracker<BaseViewHolder>?
) : ListAdapter<CategoryItem, CategoryItemsAdapter.ViewHolder>(DiffCallback()) {

    private companion object {
        const val TAG = "CategoryItemsAdapter"
    }

    private class DiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
            oldItem.category.id == newItem.category.id

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean =
            oldItem == newItem
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        if (currentList.isEmpty()) {
            return -1L
        }

        return currentList[position].category.idLong
    }

    inner class ViewHolder(
        private val binding: ItemCategoryBinding
    ) : BaseViewHolder(binding.root, selectionTracker) {

        private var oldItem: CategoryItem? = null

        override fun setupViewHolder(position: Int) {
            val item: CategoryItem = try {
                currentList[position]
            } catch (e: IndexOutOfBoundsException) {
                Log.w(TAG, e)
                return
            }

            if (item.hasCheckbox) {
                binding.chkItemCategory.visibility = View.VISIBLE
                binding.chkItemCategory.isChecked = item.isSelected
            } else {
                binding.chkItemCategory.visibility = View.GONE
                binding.chkItemCategory.isChecked = false
            }

            if (item.category != oldItem?.category) {
                binding.tvCategoryName.text = currentList[absoluteAdapterPosition].category.name

                if (item.category.color != null) {
                    binding.cdvCategoryColor.setCardBackgroundColor(item.category.color!!)
                }
            }

            oldItem = item
        }
    }
}