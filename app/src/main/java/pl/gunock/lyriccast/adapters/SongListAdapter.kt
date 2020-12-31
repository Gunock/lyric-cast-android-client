/*
 * Created by Tomasz Kiljańczyk on 10/25/20 10:05 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/25/20 9:42 PM
 */

package pl.gunock.lyriccast.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.models.SongItemModel

class SongListAdapter(
    var songs: MutableList<SongItemModel>,
    val showCheckBox: Boolean = false,
    val showRowNumber: Boolean = false,
    val showAuthor: Boolean = true
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemLayout: LinearLayout = itemView.findViewById(R.id.item_song)
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val authorTextView: TextView = itemView.findViewById(R.id.song_author)
        val categoryTextView: TextView = itemView.findViewById(R.id.song_category)
        val checkBox: CheckBox = itemView.findViewById(R.id.song_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        if (!showRowNumber) {
            holder.titleTextView.text = songs[position].title
        } else {

            holder.titleTextView.text = holder.itemView.resources
                .getString(R.string.song_item_title_template, position + 1, songs[position].title)
        }
        holder.authorTextView.text = songs[position].author
        holder.categoryTextView.text = songs[position].category

        if (!showAuthor) {
            holder.authorTextView.visibility = View.GONE
        }

        if (!showCheckBox) {
            holder.checkBox.visibility = View.GONE
        } else {
            holder.titleTextView.setPadding(
                0,
                holder.titleTextView.paddingTop,
                holder.titleTextView.paddingRight,
                holder.titleTextView.paddingBottom
            )

            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                songs[position].selected = isChecked
            }

            holder.checkBox.isChecked = songs[position].selected
        }

        if (songs[position].highlight) {
            holder.itemLayout.setBackgroundColor(
                holder.itemView.resources.getColor(
                    R.color.colorAccent,
                    null
                )
            )
        } else {
            holder.itemLayout.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount() = songs.size

}