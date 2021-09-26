/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 17:28
 */

package pl.gunock.lyriccast.ui.shared.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.databinding.ItemSongBinding
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import pl.gunock.lyriccast.ui.shared.misc.VisibilityObserver
import java.util.*

class SongItemsAdapter(
    context: Context,
    val showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    private val selectionTracker: SelectionTracker<BaseViewHolder>?
) : RecyclerView.Adapter<SongItemsAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private val mLifecycleOwner: LifecycleOwner = context.getLifecycleOwner()!!

    private val mDefaultItemCardColor = context.getColor(R.color.window_background_2)
    private val mWithCategoryTextColor = context.getColor(R.color.text_item_with_category)
    private val mNoCategoryTextColor = context.getColor(R.color.text_item_no_category)
    private val mCheckBoxColors = context.getColorStateList(R.color.checkbox_state_list)

    private val mItems: SortedSet<SongItem> = sortedSetOf()
    val songItems: List<SongItem>
        get() {
            return try {
                mItems.toList()
            } catch (e: ConcurrentModificationException) {
                listOf()
            }
        }

    fun removeObservers() {
        showCheckBox.removeObservers(mLifecycleOwner)
        mItems.forEach { it.isSelected.removeObservers(mLifecycleOwner) }
    }

    suspend fun submitCollection(songs: Iterable<SongItem>) {
        withContext(Dispatchers.Main) {
            notifyItemRangeRemoved(0, mItems.size)
        }
        withContext(Dispatchers.Default) {
            mItems.clear()
            mItems.addAll(songs)
        }
        withContext(Dispatchers.Main) {
            notifyItemRangeRemoved(0, mItems.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemId(position: Int): Long {
        val songs = songItems

        if (songs.isEmpty()) {
            return -1L
        }

        return songItems[position].song.idLong
    }

    override fun getItemCount() = mItems.size

    inner class ViewHolder(
        private val mBinding: ItemSongBinding
    ) : BaseViewHolder(mBinding.root, selectionTracker) {
        init {
            mBinding.tvSongCategory.setTextColor(this@SongItemsAdapter.mWithCategoryTextColor)
        }

        override fun setUpViewHolder(position: Int) {
            val item = songItems[position]
            showCheckBox.observe(mLifecycleOwner, VisibilityObserver(mBinding.chkItemSong))
            item.isSelected.observe(mLifecycleOwner) {
                mBinding.chkItemSong.isChecked = it
            }

            mBinding.tvItemSongTitle.text = item.song.title

            if (item.song.category != null) {
                mBinding.tvSongCategory.text = item.song.category?.name

                mBinding.chkItemSong.buttonTintList =
                    ColorStateList.valueOf(this@SongItemsAdapter.mWithCategoryTextColor)

                mBinding.tvItemSongTitle.setTextColor(this@SongItemsAdapter.mWithCategoryTextColor)
                mBinding.root.setCardBackgroundColor(item.song.category?.color!!)
            } else {
                mBinding.tvSongCategory.text = ""

                mBinding.chkItemSong.buttonTintList = mCheckBoxColors

                mBinding.tvItemSongTitle.setTextColor(mNoCategoryTextColor)
                mBinding.root.setCardBackgroundColor(this@SongItemsAdapter.mDefaultItemCardColor)
            }
        }
    }
}