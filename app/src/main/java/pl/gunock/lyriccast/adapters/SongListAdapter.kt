/*
 * Created by Tomasz Kiljańczyk on 10/19/20 12:26 AM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/18/20 11:08 PM
 */

package pl.gunock.lyriccast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.models.SongItemModel

class SongListAdapter(
    var songs: List<SongItemModel>,
    val showCheckBox: Boolean = false
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        holder.titleTextView.text = songs[position].title
        holder.authorTextView.text = songs[position].author
        holder.categoryTextView.text = songs[position].category
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
                songs[position].isSelected = isChecked
            }

            holder.checkBox.isChecked = songs[position].isSelected
        }
    }

    override fun getItemCount() = songs.size

}