/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 16:53
 */

package pl.gunock.lyriccast.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.databinding.ItemSetlistBinding
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import pl.gunock.lyriccast.ui.shared.misc.VisibilityObserver
import java.util.*

class SetlistItemsAdapter(
    context: Context,
    var showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val selectionTracker: SelectionTracker<BaseViewHolder>?
) : RecyclerView.Adapter<SetlistItemsAdapter.ViewHolder>() {

    companion object {
        const val TAG = "SetlistItemsAdapter"
    }

    private val mLifecycleOwner: LifecycleOwner = context.getLifecycleOwner()!!

    private var mItems: SortedSet<SetlistItem> = sortedSetOf()
    val setlistItems: List<SetlistItem> get() = mItems.toList()

    init {
        setHasStableIds(true)
    }

    fun removeObservers() {
        showCheckBox.removeObservers(mLifecycleOwner)
        mItems.forEach { it.isSelected.removeObservers(mLifecycleOwner) }
    }

    suspend fun submitCollection(setlists: Iterable<SetlistItem>) {
        withContext(Dispatchers.Main) {
            notifyItemRangeRemoved(0, mItems.size)
        }
        withContext(Dispatchers.Default) {
            mItems.clear()
            mItems.addAll(setlists)
        }
        withContext(Dispatchers.Main) {
            notifyItemRangeRemoved(0, mItems.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return try {
            setlistItems[position].setlist.idLong
        } catch (e: ConcurrentModificationException) {
            -1L
        }
    }

    override fun getItemCount() = mItems.size

    inner class ViewHolder(
        private val mBinding: ItemSetlistBinding
    ) : BaseViewHolder(mBinding.root, selectionTracker) {

        override fun setUpViewHolder(position: Int) {
            val item = mItems.toList()[position]

            showCheckBox.observe(mLifecycleOwner, VisibilityObserver(mBinding.chkItemSetlist))
            item.isSelected.observe(mLifecycleOwner) {
                mBinding.chkItemSetlist.isChecked = it
            }

            mBinding.tvItemSetlistName.text = item.setlist.name
        }
    }

}