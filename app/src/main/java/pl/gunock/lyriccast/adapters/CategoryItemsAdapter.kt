/*
 * Created by Tomasz Kiljanczyk on 15/05/2021, 15:20
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 15/05/2021, 15:00
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.databinding.ItemCategoryBinding
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.misc.VisibilityObserver
import pl.gunock.lyriccast.models.CategoryItem
import java.util.*

class CategoryItemsAdapter(
    context: Context,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    private val mSelectionTracker: SelectionTracker<ViewHolder>?
) : RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder>() {

    private val mLock = Any()
    private val mLifecycleOwner: LifecycleOwner = context.getLifecycleOwner()!!

    private var mItems: SortedSet<CategoryItem> = sortedSetOf()
    val categoryItems: List<CategoryItem> get() = mItems.toList()

    init {
        setHasStableIds(true)
    }

    suspend fun submitCollection(categories: RealmResults<CategoryDocument>) {
        val frozenCategories = categories.freeze()
        withContext(Dispatchers.Default) {
            mItems.clear()
            mItems.addAll(frozenCategories.map { CategoryItem(it) })
        }
        notifyDataSetChanged()
    }

    fun resetSelection() {
        mItems.forEach { it.isSelected.value = false }
        mSelectionTracker?.reset()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return categoryItems[position].category.idLong
    }

    override fun getItemCount() = mItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mBinding = ItemCategoryBinding.bind(itemView)

        fun bind(position: Int) {
            val item: CategoryItem = categoryItems[position]
            mSelectionTracker?.attach(this)

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