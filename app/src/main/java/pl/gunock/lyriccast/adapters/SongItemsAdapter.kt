/*
 * Created by Tomasz Kiljanczyk on 4/3/21 9:09 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/3/21 7:52 PM
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
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
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.misc.VisibilityObserver
import pl.gunock.lyriccast.models.SongItem
import java.util.*
import kotlin.system.measureTimeMillis

class SongItemsAdapter(
    val context: Context,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val selectionTracker: SelectionTracker<ViewHolder>?
) : RecyclerView.Adapter<SongItemsAdapter.ViewHolder>() {

    private companion object {
        val CHECKBOX_STATES = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
    }

    init {
        setHasStableIds(true)
    }

    private val lock = Any()
    private val checkBoxColor = context.getColor(R.color.checkBox)
    private val checkBoxHighlightColor = context.getColor(R.color.checkBoxHighlight)
    private val brightColor = context.getColor(R.color.white)
    private val darkColor = context.getColor(R.color.black)
    val checkBoxColors = intArrayOf(checkBoxHighlightColor, checkBoxColor)

    private var _items: SortedSet<SongItem> = sortedSetOf()
    private var _visibleItems: Set<SongItem> = setOf()
    val songItems: List<SongItem> get() = _visibleItems.toList()

    fun submitCollection(songs: Collection<SongAndCategory>) {
        synchronized(lock) {
            _items.clear()
            _items.addAll(songs.map { SongItem(it) })
            _visibleItems = _items
            notifyDataSetChanged()
        }
    }

    fun filterItems(
        songTitle: String,
        categoryId: Long = Long.MIN_VALUE,
        isSelected: Boolean? = null
    ) {
        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (isSelected != null) {
            predicates.add { songItem -> songItem.isSelected.value!! }
        }

        if (categoryId != Long.MIN_VALUE) {
            predicates.add { songItem -> songItem.category?.categoryId == categoryId }
        }

        val normalizedTitle = songTitle.trim().normalize()
        predicates.add { item ->
            item.normalizedTitle.contains(normalizedTitle, ignoreCase = true)
        }

        val duration = measureTimeMillis {
            _visibleItems = _items.filter { songItem ->
                predicates.all { predicate -> predicate(songItem) }
            }.toSortedSet()
        }
        Log.v(SetlistItemsAdapter.TAG, "Filtering took : ${duration}ms")
        notifyDataSetChanged()
    }

    fun resetSelection() {
        _visibleItems.forEach { it.isSelected.value = false }
        selectionTracker?.reset()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        return _visibleItems.toList()[position].song.id
    }

    override fun getItemCount() = _visibleItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_song)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tv_song_category)

        init {
            checkBox
            categoryTextView.setTextColor(this@SongItemsAdapter.brightColor)
        }

        fun bind(position: Int) {
            selectionTracker?.attach(this)
            showCheckBox.observe(context.getLifecycleOwner()!!, VisibilityObserver(checkBox))

            val item = _visibleItems.toList()[position]
            titleTextView.text = item.song.title

            if (item.category != null) {
                categoryTextView.text = item.category.name
                checkBox.buttonTintList = ColorStateList.valueOf(this@SongItemsAdapter.brightColor)
                titleTextView.setTextColor(this@SongItemsAdapter.brightColor)
                (itemView as CardView).setCardBackgroundColor(item.category.color!!)
            } else {
                categoryTextView.text = ""

                checkBox.buttonTintList = ColorStateList(CHECKBOX_STATES, checkBoxColors)

                titleTextView.setTextColor(darkColor)
                (itemView as CardView).setCardBackgroundColor(this@SongItemsAdapter.brightColor)
            }

            item.isSelected.observe(context.getLifecycleOwner()!!) {
                checkBox.isChecked = it
            }

        }
    }
}