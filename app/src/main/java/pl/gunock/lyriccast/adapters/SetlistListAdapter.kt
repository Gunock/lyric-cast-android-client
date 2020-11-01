/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 3:30 PM
 */

package pl.gunock.lyriccast.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.models.SetlistItemModel

class SetlistListAdapter(var setlists: MutableList<SetlistItemModel>) :
    RecyclerView.Adapter<SetlistListAdapter.SetlistViewHolder>() {

    class SetlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val authorTextView: TextView = itemView.findViewById(R.id.song_author)
        val categoryCardView: CardView = itemView.findViewById(R.id.card_song_category)
        val checkBox: CheckBox = itemView.findViewById(R.id.song_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetlistViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return SetlistViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SetlistViewHolder, position: Int) {
        holder.titleTextView.text = setlists[position].name
        holder.authorTextView.text = ""
        holder.categoryCardView.visibility = View.GONE
        holder.checkBox.visibility = View.GONE
    }

    override fun getItemCount() = setlists.size
}