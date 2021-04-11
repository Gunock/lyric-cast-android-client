/*
 * Created by Tomasz Kiljanczyk on 4/11/21 2:05 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 1:55 AM
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.misc.VisibilityObserver
import pl.gunock.lyriccast.models.SetlistItem
import java.util.*
import kotlin.system.measureTimeMillis

class SetlistItemsAdapter(
    context: Context,
    var showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val selectionTracker: SelectionTracker<ViewHolder>?
) : RecyclerView.Adapter<SetlistItemsAdapter.ViewHolder>() {

    companion object {
        const val TAG = "SetlistItemsAdapter"
    }

    private val mLock = Any()
    private val mLifecycleOwner: LifecycleOwner = context.getLifecycleOwner()!!

    private var mItems: SortedSet<SetlistItem> = sortedSetOf()
    private var mVisibleItems: Set<SetlistItem> = setOf()
    val setlistItems: List<SetlistItem> get() = mItems.toList()

    init {
        setHasStableIds(true)
    }

    fun removeObservers() {
        showCheckBox.removeObservers(mLifecycleOwner)
        mItems.forEach { it.isSelected.removeObservers(mLifecycleOwner) }
    }

    fun submitCollection(setlistWithSongs: Collection<Setlist>) {
        synchronized(mLock) {
            mItems.clear()
            mItems.addAll(setlistWithSongs.map { SetlistItem(it) })
            mVisibleItems = mItems
            notifyDataSetChanged()
        }
    }

    fun filterItems(setlistName: String) {
        val normalizedName = setlistName.trim().normalize()

        val duration = measureTimeMillis {
            mVisibleItems = mItems.filter { item ->
                item.normalizedName.contains(normalizedName, ignoreCase = true)
            }.toSortedSet()
        }
        Log.v(TAG, "Filtering took : ${duration}ms")
        notifyDataSetChanged()
    }

    fun resetSelection() {
        mVisibleItems.forEach { it.isSelected.value = false }
        selectionTracker?.reset()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setlist, parent, false)

        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return mVisibleItems.toList()[position].setlist.id
    }

    override fun getItemCount() = mVisibleItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mCheckBox: CheckBox = itemView.findViewById(R.id.chk_item_setlist)
        private val mNameTextView: TextView = itemView.findViewById(R.id.tv_item_setlist_name)

        fun bind(position: Int) {
            val item = mVisibleItems.toList()[position]
            selectionTracker?.attach(this)

            showCheckBox.observe(mLifecycleOwner, VisibilityObserver(mCheckBox))
            item.isSelected.observe(mLifecycleOwner) {
                mCheckBox.isChecked = it
            }

            mNameTextView.text = item.setlist.name
        }
    }

}