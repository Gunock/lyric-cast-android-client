/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 17:28
 */

package pl.gunock.lyriccast.ui.category_manager

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.databinding.ItemCategoryBinding
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import pl.gunock.lyriccast.ui.shared.misc.VisibilityObserver
import java.util.*

class CategoryItemsAdapter(
    context: Context,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    private val mSelectionTracker: SelectionTracker<BaseViewHolder>?
) : RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder>() {

    private val mLifecycleOwner: LifecycleOwner = context.getLifecycleOwner()!!

    private var mItems: SortedSet<CategoryItem> = sortedSetOf()
    val categoryItems: List<CategoryItem> get() = mItems.toList()

    init {
        setHasStableIds(true)
    }

    suspend fun submitCollection(categories: List<Category>) {
        withContext(Dispatchers.Main) {
            notifyItemRangeRemoved(0, mItems.size)
        }
        withContext(Dispatchers.Default) {
            mItems.clear()
            mItems.addAll(categories.map { CategoryItem(it) })
        }
        withContext(Dispatchers.Main) {
            notifyItemRangeInserted(0, mItems.size)
        }
    }

    fun resetSelection() {
        mItems.forEach { it.isSelected.postValue(false) }
        mSelectionTracker?.reset()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return try {
            categoryItems[position].category.idLong
        } catch (e: ConcurrentModificationException) {
            -1L
        }
    }

    override fun getItemCount() = mItems.size

    inner class ViewHolder(
        private val mBinding: ItemCategoryBinding
    ) : BaseViewHolder(mBinding.root, mSelectionTracker) {
        override fun setUpViewHolder(position: Int) {
            val item: CategoryItem = categoryItems[position]

            showCheckBox.observe(
                mLifecycleOwner,
                VisibilityObserver(mBinding.cdvCategoryColor, true)
            )
            showCheckBox.observe(mLifecycleOwner, VisibilityObserver(mBinding.chkItemCategory))
            item.isSelected.observe(mLifecycleOwner) {
                mBinding.chkItemCategory.isChecked = it
            }

            mBinding.tvCategoryName.text = categoryItems[absoluteAdapterPosition].category.name

            if (item.category.color != null) {
                mBinding.cdvCategoryColor.setCardBackgroundColor(item.category.color!!)
            }

        }
    }

}