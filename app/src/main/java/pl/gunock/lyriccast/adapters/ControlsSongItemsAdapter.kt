/*
 * Created by Tomasz Kiljanczyk on 4/5/21 11:56 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 11:53 PM
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
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.getLifecycleOwner
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SongItem


class ControlsSongItemsAdapter(
    private val mContext: Context,
    val songItems: List<SongItem>,
    val onItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<ControlsSongItemsAdapter.ViewHolder>() {

    private companion object {
        const val ANIMATION_DURATION: Long = 400L
    }

    private val mCardHighlightColor = mContext.getColor(R.color.colorAccent)
    private val mBrightColor = mContext.getColor(R.color.white)
    private val mDarkColor = mContext.getColor(R.color.black)

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
        private var mCurrentCardColor = mBrightColor

        fun bind(position: Int) {
            val item: SongItem = songItems[position]
            val titleText = itemView.context.resources.getString(
                R.string.setlist_controls_song_item_title_template,
                adapterPosition + 1,
                item.song.title
            )
            mTitleTextView.text = titleText

            setupListeners()
        }

        private fun setupListeners() {
            songItems[adapterPosition].highlight
                .observe(mContext.getLifecycleOwner()!!, this::observeHighlight)

            if (onItemLongClickListener != null) {
                mItemCardView.setOnLongClickListener { view ->
                    onItemLongClickListener.execute(this, adapterPosition, view)
                }
            }

            if (onItemClickListener != null) {
                mItemCardView.setOnClickListener { view ->
                    onItemClickListener.execute(this, adapterPosition, view)
                }
            }
        }

        private fun observeHighlight(value: Boolean) {
            val cardTo: Int
            val textTo: Int
            if (value) {
                cardTo = mCardHighlightColor
                textTo = mBrightColor
            } else {
                cardTo = mBrightColor
                textTo = mDarkColor
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
                    mItemCardView.setBackgroundColor(animator.animatedValue as Int)
                }

                duration = ANIMATION_DURATION
                start()
            }

            mCurrentCardColor = cardTo
        }
    }
}