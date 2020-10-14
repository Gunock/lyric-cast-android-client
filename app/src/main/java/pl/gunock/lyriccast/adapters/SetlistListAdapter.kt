/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/14/20 11:41 PM
 */

package pl.gunock.lyriccast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.models.SetlistModel

class SetlistListAdapter(var setlists: MutableList<SetlistModel>) :
    RecyclerView.Adapter<SetlistListAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val authorTextView: TextView = itemView.findViewById(R.id.song_author)
        val categoryCardView: CardView = itemView.findViewById(R.id.card_song_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return SongViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.titleTextView.text = setlists[position].name
        holder.authorTextView.text = ""
        holder.categoryCardView.visibility = View.INVISIBLE
    }

    override fun getItemCount() = setlists.size
}