/*
 * Created by Tomasz Kiljanczyk on 4/8/21 6:49 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/8/21 6:35 PM
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
    private val mContext: Context,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    private val mSelectionTracker: SelectionTracker<ViewHolder>?
) : RecyclerView.Adapter<SongItemsAdapter.ViewHolder>() {

    private companion object {
        val CHECKBOX_STATES = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
    }

    init {
        setHasStableIds(true)
    }

    private val mLock = Any()

    private val mCheckBoxColor = mContext.getColor(R.color.check_box)
    private val mCheckBoxHighlightColor = mContext.getColor(R.color.check_box_highlight)
    private val mDefaultItemCardColor = mContext.getColor(R.color.window_background_2)
    private val mWithCategoryTextColor = mContext.getColor(R.color.text_item_with_category)
    private val mNoCategoryTextColor = mContext.getColor(R.color.text_item_no_category)
    private val mCheckBoxColors = intArrayOf(mCheckBoxHighlightColor, mCheckBoxColor)

    private var mItems: SortedSet<SongItem> = sortedSetOf()
    private var mVisibleItems: Set<SongItem> = setOf()
    val songItems: List<SongItem> get() = mVisibleItems.toList()

    fun submitCollection(songs: Collection<SongAndCategory>) {
        synchronized(mLock) {
            mItems.clear()
            mItems.addAll(songs.map { SongItem(it) })
            mVisibleItems = mItems
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
            mVisibleItems = mItems.filter { songItem ->
                predicates.all { predicate -> predicate(songItem) }
            }.toSortedSet()
        }
        Log.v(SetlistItemsAdapter.TAG, "Filtering took : ${duration}ms")
        notifyDataSetChanged()
    }

    fun resetSelection() {
        mVisibleItems.forEach { it.isSelected.value = false }
        mSelectionTracker?.reset()
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
        return mVisibleItems.toList()[position].song.id
    }

    override fun getItemCount() = mVisibleItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mCheckBox: CheckBox = itemView.findViewById(R.id.chk_item_song)
        private val mTitleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)
        private val mCategoryTextView: TextView = itemView.findViewById(R.id.tv_song_category)

        init {
            mCategoryTextView.setTextColor(this@SongItemsAdapter.mWithCategoryTextColor)
        }

        fun bind(position: Int) {
            val item = mVisibleItems.toList()[position]
            mSelectionTracker?.attach(this)
            showCheckBox.observe(mContext.getLifecycleOwner()!!, VisibilityObserver(mCheckBox))
            item.isSelected.observe(mContext.getLifecycleOwner()!!) {
                mCheckBox.isChecked = it
            }

            mTitleTextView.text = item.song.title

            if (item.category != null) {
                mCategoryTextView.text = item.category.name
                mCheckBox.buttonTintList =
                    ColorStateList.valueOf(this@SongItemsAdapter.mWithCategoryTextColor)
                mTitleTextView.setTextColor(this@SongItemsAdapter.mWithCategoryTextColor)
                (itemView as CardView).setCardBackgroundColor(item.category.color!!)
            } else {
                mCategoryTextView.text = ""

                mCheckBox.buttonTintList = ColorStateList(CHECKBOX_STATES, mCheckBoxColors)

                mTitleTextView.setTextColor(mNoCategoryTextColor)
                (itemView as CardView).setCardBackgroundColor(this@SongItemsAdapter.mDefaultItemCardColor)
            }
        }
    }
}