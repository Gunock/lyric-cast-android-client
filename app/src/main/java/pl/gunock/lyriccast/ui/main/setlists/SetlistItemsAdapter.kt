/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 21:22
 */

package pl.gunock.lyriccast.ui.main.setlists

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.databinding.ItemSetlistBinding
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class SetlistItemsAdapter(
    context: Context,
    val selectionTracker: SelectionTracker<BaseViewHolder>?
) : RecyclerView.Adapter<SetlistItemsAdapter.ViewHolder>() {

    companion object {
        const val TAG = "SetlistItemsAdapter"
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private var _items: List<SetlistItem> = listOf()

    init {
        setHasStableIds(true)
    }

    fun submitCollection(setlists: List<SetlistItem>) {
        val previousSize = itemCount
        _items = setlists
        notifyItemRangeRemoved(0, previousSize)
        notifyItemRangeRemoved(0, _items.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetlistBinding.inflate(inflater, parent, false)
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