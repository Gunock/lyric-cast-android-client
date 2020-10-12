/*
 * Created by Tomasz Kiljańczyk on 10/12/20 10:37 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/12/20 10:11 PM
 */

package pl.gunock.lyriccast

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.models.SongMetadataModel

class SongListAdapter(var songs: MutableList<SongMetadataModel>) :
    RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val authorTextView: TextView = itemView.findViewById(R.id.song_author)
        val tagTextView: TextView = itemView.findViewById(R.id.song_tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.titleTextView.text = songs[position].title
        holder.authorTextView.text = songs[position].author
        if (songs[position].tags.isNotEmpty()) {
            holder.tagTextView.text = songs[position].tags[0]
        } else {
            holder.tagTextView.text = ""
        }
    }

    override fun getItemCount() = songs.size
}