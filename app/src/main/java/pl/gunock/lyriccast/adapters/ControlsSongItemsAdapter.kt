/*
 * Created by Tomasz Kiljanczyk on 4/11/21 2:05 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 12:09 AM
 */

package pl.gunock.lyriccast.adapters

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SongItem


class ControlsSongItemsAdapter(
    context: Context,
    val songItems: List<SongItem>,
    private val mOnItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    private val mOnItemClickListener: ClickAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<ControlsSongItemsAdapter.ViewHolder>() {

    private companion object {
        const val ANIMATION_DURATION: Long = 400L
    }

    private val mLifecycleOwner: LifecycleOwner = context.getLifecycleOwner()!!

    private val mCardHighlightColor = context.getColor(R.color.accent)
    private val mDefaultItemCardColor = context.getColor(R.color.window_background_1)
    private val mTextDefaultColor = context.getColor(R.color.text)
    private val mTextHighlightColor = context.getColor(R.color.button_text_2)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_controls_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = songItems.size

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val mItemCardView: CardView = itemView.findViewById(R.id.item_song)
        private val mTitleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)
        private var mCurrentCardColor = mDefaultItemCardColor

        fun bind(position: Int) {
            val item: SongItem = songItems[position]
            val titleText = itemView.context.resources.getString(
                R.string.setlist_controls_song_item_title_template,
                absoluteAdapterPosition + 1,
                item.song.title
            )
            mTitleTextView.text = titleText

            setupListeners()
        }

        private fun setupListeners() {
            songItems[absoluteAdapterPosition].highlight
                .observe(mLifecycleOwner, this::observeHighlight)

            if (mOnItemLongClickListener != null) {
                mItemCardView.setOnLongClickListener { view ->
                    mOnItemLongClickListener.execute(this, absoluteAdapterPosition, view)
                }
            }

            if (mOnItemClickListener != null) {
                mItemCardView.setOnClickListener { view ->
                    mOnItemClickListener.execute(this, absoluteAdapterPosition, view)
                }
            }
        }

        private fun observeHighlight(value: Boolean) {
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
                setIntValues(mTitleTextView.currentTextColor, textTo)
                setEvaluator(ArgbEvaluator())
                addUpdateListener { animator ->
                    mTitleTextView.setTextColor(animator.animatedValue as Int)
                }

                duration = ANIMATION_DURATION
                start()
            }

            with(ValueAnimator()) {
                setIntValues(mCurrentCardColor, cardTo)
                setEvaluator(ArgbEvaluator())
                addUpdateListener { animator ->
                    mItemCardView.setCardBackgroundColor(animator.animatedValue as Int)
                }

                duration = ANIMATION_DURATION
                start()
            }

            mCurrentCardColor = cardTo
        }
    }
}