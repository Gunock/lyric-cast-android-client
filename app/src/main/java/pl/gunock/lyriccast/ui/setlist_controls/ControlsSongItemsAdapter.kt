/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 11:38
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 10:54
 */

package pl.gunock.lyriccast.ui.setlist_controls

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ItemControlsSongBinding
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.ui.shared.listeners.LongClickAdapterItemListener


class ControlsSongItemsAdapter(
    context: Context,
    val songItems: List<SongItem>,
    private val mOnItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    private val mOnItemClickListener: ClickAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<ControlsSongItemsAdapter.ViewHolder>() {

    private companion object {
        const val ANIMATION_DURATION: Long = 400L
    }

    private val mCardHighlightColor = context.getColor(R.color.accent)
    private val mDefaultItemCardColor = context.getColor(R.color.window_background_2)
    private val mTextDefaultColor = context.getColor(R.color.text)
    private val mTextHighlightColor = context.getColor(R.color.button_text_2)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemControlsSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = songItems.size

    inner class ViewHolder(
        private val mBinding: ItemControlsSongBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {
        private var mCurrentCardColor = mDefaultItemCardColor

        fun bind(position: Int) {
            val item: SongItem = songItems[position]
            val titleText = itemView.context.resources.getString(
                R.string.setlist_controls_song_item_title_template,
                absoluteAdapterPosition + 1,
                item.song.title
            )
            mBinding.tvItemSongTitle.text = titleText

            applyHighlight(songItems[absoluteAdapterPosition].highlight)

            setupListeners()
        }

        private fun setupListeners() {
            if (mOnItemLongClickListener != null) {
                mBinding.root.setOnLongClickListener { view ->
                    mOnItemLongClickListener.execute(this, absoluteAdapterPosition, view)
                }
            }

            if (mOnItemClickListener != null) {
                mBinding.root.setOnClickListener { view ->
                    mOnItemClickListener.execute(this, absoluteAdapterPosition, view)
                }
            }
        }

        private fun applyHighlight(value: Boolean) {
            val cardTo: Int
            val textTo: Int
            if (value) {
                cardTo = mCardHighlightColor
                textTo = mTextHighlightColor
            } else {
                cardTo = mDefaultItemCardColor
                textTo = mTextDefaultColor
            }

            with(ValueAnimator()) {
                setIntValues(mBinding.tvItemSongTitle.currentTextColor, textTo)
                setEvaluator(ArgbEvaluator())
                addUpdateListener { animator ->
                    mBinding.tvItemSongTitle.setTextColor(animator.animatedValue as Int)
                }

                duration = ANIMATION_DURATION
                start()
            }

            with(ValueAnimator()) {
                setIntValues(mCurrentCardColor, cardTo)
                setEvaluator(ArgbEvaluator())
                addUpdateListener { animator ->
                    mBinding.root.setCardBackgroundColor(animator.animatedValue as Int)
                }

                duration = ANIMATION_DURATION
                start()
            }

            mCurrentCardColor = cardTo
        }
    }
}