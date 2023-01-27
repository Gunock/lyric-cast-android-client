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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import pl.gunock.lyriccast.databinding.ItemSetlistBinding
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker

class SetlistItemsAdapter(
    context: Context,
    val selectionTracker: SelectionTracker<BaseViewHolder>?
) : ListAdapter<SetlistItem, SetlistItemsAdapter.ViewHolder>(DiffCallback()) {

    companion object {
        const val TAG = "SetlistItemsAdapter"
    }

    private class DiffCallback : DiffUtil.ItemCallback<SetlistItem>() {
        override fun areItemsTheSame(oldItem: SetlistItem, newItem: SetlistItem): Boolean =
            oldItem.setlist.id == newItem.setlist.id

        override fun areContentsTheSame(oldItem: SetlistItem, newItem: SetlistItem): Boolean =
            oldItem == newItem

    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetlistBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        if (currentList.isEmpty()) {
            return -1L
        }
        return currentList[position].setlist.idLong
    }

    inner class ViewHolder(
        private val binding: ItemSetlistBinding
    ) : BaseViewHolder(binding.root, selectionTracker) {

        private var oldItem: SetlistItem? = null

        override fun setupViewHolder(position: Int) {
            val item = try {
                currentList[position]
            } catch (e: IndexOutOfBoundsException) {
                Log.w(TAG, e)
                return
            }

            if (item.hasCheckbox) {
                binding.chkItemSetlist.visibility = View.VISIBLE
                binding.chkItemSetlist.isChecked = item.isSelected
            } else {
                binding.chkItemSetlist.visibility = View.GONE
                binding.chkItemSetlist.isChecked = false
            }

            if (item.setlist != oldItem?.setlist) {
                binding.tvItemSetlistName.text = item.setlist.name
            }

            oldItem = item
        }
    }

}