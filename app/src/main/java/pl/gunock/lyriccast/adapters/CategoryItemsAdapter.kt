/*
 * Created by Tomasz Kiljanczyk on 4/1/21 10:53 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 8:58 PM
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

    private val lock = Any()
    private var _items: SortedSet<CategoryItem> = sortedSetOf()
    val categoryItems: List<CategoryItem> get() = _items.toList()

    init {
        setHasStableIds(true)
    }

    fun submitCollection(songs: Collection<Category>) {
        synchronized(lock) {
            _items.clear()
            _items.addAll(songs.map { CategoryItem(it) })
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categoryItems[position]
        holder.bind(item)
    }

    override fun getItemId(position: Int): Long {
        return categoryItems[position].category.id
    }

    override fun getItemCount() = _items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_category)
        private val name: TextView = itemView.findViewById(R.id.tv_category_name)
        private val colorCard: CardView = itemView.findViewById(R.id.cdv_category_color)

        fun bind(item: CategoryItem) {
            selectionTracker?.attach(this)
            showCheckBox.observe(context.getLifecycleOwner()!!, VisibilityObserver(checkBox))
            showCheckBox.observe(context.getLifecycleOwner()!!, VisibilityObserver(colorCard, true))

            name.text = categoryItems[adapterPosition].category.name

            if (!showCheckBox.value!!) {
                checkBox.visibility = View.GONE
            } else {
                checkBox.visibility = View.VISIBLE
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    item.isSelected = isChecked
                }

                checkBox.isChecked = item.isSelected
            }

            if (item.category.color != null) {
                colorCard.setCardBackgroundColor(item.category.color!!)
            }

        }
    }

}