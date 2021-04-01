/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/31/21 3:04 PM
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
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
    private val context: Context,
    var showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val selectionTracker: SelectionTracker<ViewHolder>?
) : RecyclerView.Adapter<SetlistItemsAdapter.ViewHolder>() {

    private var _items: SortedSet<SetlistItem> = sortedSetOf()
    private var _visibleItems: Set<SetlistItem> = setOf()
    val setlistItems: List<SetlistItem> get() = _items.toList()

    companion object {
        const val TAG = "SetlistItemsAdapter"
    }

    init {
        setHasStableIds(true)
    }

    fun submitCollection(setlistWithSongs: Collection<Setlist>) {
        _items.clear()
        _items.addAll(setlistWithSongs.map { SetlistItem(it) })
        _visibleItems = _items
        notifyDataSetChanged()
    }

    fun filterItems(setlistName: String) {
        val normalizedName = setlistName.trim().normalize()

        val duration = measureTimeMillis {
            _visibleItems = _items.filter { item ->
                item.normalizedName.contains(normalizedName, ignoreCase = true)
            }.toSortedSet()
        }
        Log.v(TAG, "Filtering took : ${duration}ms")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setlist, parent, false)

        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemId(position: Int): Long {
        return _visibleItems.toList()[position].setlist.id
    }

    override fun getItemCount() = _visibleItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_setlist)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_item_setlist_name)

        fun bind() {
            val item = _visibleItems.toList()[adapterPosition]
            selectionTracker?.attach(this)

            showCheckBox.observe(context.getLifecycleOwner()!!, VisibilityObserver(checkBox))

            nameTextView.text = item.setlist.name
        }
    }

}