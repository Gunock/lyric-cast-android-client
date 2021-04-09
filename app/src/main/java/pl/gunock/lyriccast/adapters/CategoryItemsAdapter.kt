/*
 * Created by Tomasz Kiljanczyk on 4/9/21 11:51 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/9/21 11:08 PM
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.misc.VisibilityObserver
import pl.gunock.lyriccast.models.CategoryItem
import java.util.*

class CategoryItemsAdapter(
    val context: Context,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val selectionTracker: SelectionTracker<ViewHolder>?
) : RecyclerView.Adapter<CategoryItemsAdapter.ViewHolder>() {

    private val mLock = Any()
    private var mItems: SortedSet<CategoryItem> = sortedSetOf()
    val categoryItems: List<CategoryItem> get() = mItems.toList()

    init {
        setHasStableIds(true)
    }

    fun submitCollection(songs: Collection<Category>) {
        synchronized(mLock) {
            mItems.clear()
            mItems.addAll(songs.map { CategoryItem(it) })
            notifyDataSetChanged()
        }
    }

    fun resetSelection() {
        mItems.forEach { it.isSelected.value = false }
        selectionTracker?.reset()
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
        return categoryItems[position].category.id
    }

    override fun getItemCount() = mItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mCheckBox: CheckBox = itemView.findViewById(R.id.chk_item_category)
        private val mNameTextView: TextView = itemView.findViewById(R.id.tv_category_name)
        private val mColorCardView: CardView = itemView.findViewById(R.id.cdv_category_color)

        fun bind(position: Int) {
            val item: CategoryItem = categoryItems[position]
            selectionTracker?.attach(this)

            showCheckBox.observe(
                context.getLifecycleOwner()!!,
                VisibilityObserver(mColorCardView, true)
            )
            showCheckBox.observe(context.getLifecycleOwner()!!, VisibilityObserver(mCheckBox))
            item.isSelected.observe(context.getLifecycleOwner()!!) {
                mCheckBox.isChecked = it
            }

            mNameTextView.text = categoryItems[absoluteAdapterPosition].category.name

            if (item.category.color != null) {
                mColorCardView.setCardBackgroundColor(item.category.color!!)
            }

        }
    }

}