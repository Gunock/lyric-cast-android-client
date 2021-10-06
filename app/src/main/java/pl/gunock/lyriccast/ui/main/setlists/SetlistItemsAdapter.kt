/*
 * Created by Tomasz Kiljanczyk on 06/10/2021, 20:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/10/2021, 20:13
 */

package pl.gunock.lyriccast.ui.main.setlists

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.databinding.ItemSetlistBinding
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class SetlistItemsAdapter(
    val selectionTracker: SelectionTracker<BaseViewHolder>?
) : RecyclerView.Adapter<SetlistItemsAdapter.ViewHolder>() {

    companion object {
        const val TAG = "SetlistItemsAdapter"
    }

    private var _items: MutableList<SetlistItem> = mutableListOf()

    init {
        setHasStableIds(true)
    }

    suspend fun submitCollection(setlists: List<SetlistItem>) {
        val previousSize = itemCount
        withContext(Dispatchers.Default) {
            _items.clear()
            _items.addAll(setlists)
        }
        withContext(Dispatchers.Main) {
            notifyItemRangeRemoved(0, previousSize)
            notifyItemRangeRemoved(0, _items.size)
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
        if (_items.isEmpty()) {
            return -1L
        }
        return _items[position].setlist.idLong
    }

    override fun getItemCount() = _items.size

    inner class ViewHolder(
        private val binding: ItemSetlistBinding
    ) : BaseViewHolder(binding.root, selectionTracker) {

        override fun setUpViewHolder(position: Int) {
            val item = try {
                _items[position]
            } catch (e: IndexOutOfBoundsException) {
                Log.w(TAG, e)
                return
            }

            if (item.hasCheckbox) {
                binding.chkItemSetlist.visibility = View.VISIBLE
                binding.chkItemSetlist.isChecked = item.isSelected
            } else {
                binding.chkItemSetlist.visibility = View.GONE
            }

            binding.tvItemSetlistName.text = item.setlist.name
        }
    }

}