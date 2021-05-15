/*
 * Created by Tomasz Kiljanczyk on 15/05/2021, 15:20
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 15/05/2021, 14:53
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.util.Log
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
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.databinding.ItemSetlistBinding
import pl.gunock.lyriccast.datamodel.documents.SetlistDocument
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

    suspend fun submitCollection(setlists: RealmResults<SetlistDocument>) {
        val frozenSetlists = setlists.freeze()
        withContext(Dispatchers.Default) {
            mItems.clear()
            mItems.addAll(frozenSetlists.map { SetlistItem(it) })
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
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setlist, parent, false)

        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return mVisibleItems.toList()[position].setlist.idLong
    }

    override fun getItemCount() = mVisibleItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mBinding = ItemSetlistBinding.bind(itemView)

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