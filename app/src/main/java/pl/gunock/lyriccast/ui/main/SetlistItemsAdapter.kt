/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 14:32
 */

package pl.gunock.lyriccast.ui.main

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.databinding.ItemSetlistBinding
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import pl.gunock.lyriccast.ui.shared.misc.VisibilityObserver
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

    suspend fun submitCollection(setlists: Iterable<Setlist>) {
        withContext(Dispatchers.Default) {
            mItems.clear()
            mItems.addAll(setlists.map { SetlistItem(it) })
            mVisibleItems = mItems
        }
        notifyDataSetChanged()
    }

    suspend fun filterItems(setlistName: String) {
        withContext(Dispatchers.Default) {
            val normalizedName = setlistName.trim().normalize()

            val duration = measureTimeMillis {
                mVisibleItems = mItems.filter { item ->
                    item.normalizedName.contains(normalizedName, ignoreCase = true)
                }.toSortedSet()
            }
            Log.v(TAG, "Filtering took : ${duration}ms")
        }
        notifyDataSetChanged()
    }

    fun resetSelection() {
        mVisibleItems.forEach { it.isSelected.value = false }
        selectionTracker?.reset()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return mVisibleItems.toList()[position].setlist.idLong
    }

    override fun getItemCount() = mVisibleItems.size

    inner class ViewHolder(
        private val mBinding: ItemSetlistBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        fun bind(position: Int) {
            val item = mVisibleItems.toList()[position]
            selectionTracker?.attach(this)

            showCheckBox.observe(mLifecycleOwner, VisibilityObserver(mBinding.chkItemSetlist))
            item.isSelected.observe(mLifecycleOwner) {
                mBinding.chkItemSetlist.isChecked = it
            }

            mBinding.tvItemSetlistName.text = item.setlist.name
        }
    }

}