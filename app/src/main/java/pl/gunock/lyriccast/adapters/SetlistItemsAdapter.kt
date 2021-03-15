/*
 * Created by Tomasz Kiljańczyk on 3/15/21 1:22 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 1:20 AM
 */

package pl.gunock.lyriccast.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.extensions.getLifecycleOwner
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SetlistItem

class SetlistItemsAdapter(
    val context: Context,
    var setlistItems: MutableList<SetlistItem>,
    var showCheckBox: MutableLiveData<Boolean> = MutableLiveData(false),
    val onItemLongClickListener: LongClickAdapterItemListener<SetlistViewHolder>? = null,
    val onItemClickListener: ClickAdapterItemListener<SetlistViewHolder>? = null
) : RecyclerView.Adapter<SetlistItemsAdapter.SetlistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetlistViewHolder {
        val textView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setlist, parent, false)

        return SetlistViewHolder(textView)
    }

    override fun onBindViewHolder(holder: SetlistViewHolder, position: Int) {
        val item = setlistItems[position]
        holder.bind(item)
    }

    override fun getItemCount() = setlistItems.size

    inner class SetlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.chk_item_setlist)
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_item_setlist_name)

        fun bind(item: SetlistItem) {
            setupListeners()
            showCheckBox.observe(context.getLifecycleOwner()!!, this::observeShowCheckbox)
            nameTextView.text = setlistItems[adapterPosition].name

            if (!showCheckBox.value!!) {
                checkBox.visibility = View.GONE
            } else {
                checkBox.visibility = View.VISIBLE
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    item.isSelected = isChecked
                }

                checkBox.isChecked = item.isSelected
            }
        }

        private fun observeShowCheckbox(value: Boolean) {
            if (value) {
                checkBox.visibility = View.VISIBLE
            } else {
                checkBox.visibility = View.GONE
            }
        }

        private fun setupListeners() {
            if (onItemLongClickListener != null) {
                itemView.setOnLongClickListener { view ->
                    onItemLongClickListener.execute(this, adapterPosition, view)
                }
            }

            if (onItemClickListener != null) {
                itemView.setOnClickListener { view ->
                    onItemClickListener.execute(this, adapterPosition, view)
                }
            }
        }
    }
}