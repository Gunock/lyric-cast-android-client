/*
 * Created by Tomasz Kiljańczyk on 3/17/21 12:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/16/21 11:56 PM
 */

package pl.gunock.lyriccast.adapters

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.extensions.getLifecycleOwner
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SongItem


class ControlsSongItemsAdapter(
    private val context: Context,
    val songItems: List<SongItem>,
    val onItemLongClickListener: LongClickAdapterItemListener<ViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<ViewHolder>? = null
) : RecyclerView.Adapter<ControlsSongItemsAdapter.ViewHolder>() {

    private companion object {
        const val ANIMATION_DURATION: Long = 400L
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_controls_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = songItems[position]

        holder.bind(item)
    }

    override fun getItemCount() = songItems.size

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_item_song_title)
        private val itemCardView: CardView = itemView.findViewById(R.id.item_song)
        private val defaultBackgroundColor = itemCardView.cardBackgroundColor.defaultColor
        private val defaultFontColor = titleTextView.currentTextColor
        private var currentCardColor = defaultBackgroundColor

        fun bind(item: SongItem) {
            val titleText = itemView.context.resources.getString(
                R.string.item_song_item_title_template,
                adapterPosition + 1,
                item.title
            )
            titleTextView.text = titleText

            setupListeners()
        }

        private fun setupListeners() {
            songItems[adapterPosition].highlight
                .observe(context.getLifecycleOwner()!!, this::observeHighlight)

            if (onItemLongClickListener != null) {
                itemCardView.setOnLongClickListener { view ->
                    onItemLongClickListener.execute(this, adapterPosition, view)
                }
            }

            if (onItemClickListener != null) {
                itemCardView.setOnClickListener { view ->
                    onItemClickListener.execute(this, adapterPosition, view)
                }
            }
        }

        private fun observeHighlight(value: Boolean) {
            val cardTo: Int
            val textTo: Int
            if (value) {
                cardTo = context.getColor(R.color.colorAccent)
                textTo = Color.WHITE
            } else {
                cardTo = defaultBackgroundColor
                textTo = defaultFontColor
            }

            with(ValueAnimator()) {
                setIntValues(titleTextView.currentTextColor, textTo)
                setEvaluator(ArgbEvaluator())
                addUpdateListener { animator ->
                    titleTextView.setTextColor(animator.animatedValue as Int)
                }

                duration = ANIMATION_DURATION
                start()
            }

            with(ValueAnimator()) {
                setIntValues(currentCardColor, cardTo)
                setEvaluator(ArgbEvaluator())
                addUpdateListener { animator ->
                    itemCardView.setBackgroundColor(animator.animatedValue as Int)
                }

                duration = ANIMATION_DURATION
                start()
            }

            currentCardColor = cardTo
        }
    }
}