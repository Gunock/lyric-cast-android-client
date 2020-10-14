/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/13/20 9:12 PM
 */

package pl.gunock.lyriccast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.models.SongMetadataModel

class SongListAdapter(var songs: MutableList<SongMetadataModel>) :
    RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val authorTextView: TextView = itemView.findViewById(R.id.song_author)
        val categoryTextView: TextView = itemView.findViewById(R.id.song_category)
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
    }

    override fun getItemCount() = songs.size
}