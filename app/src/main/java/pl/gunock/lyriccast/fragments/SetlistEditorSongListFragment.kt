/*
 * Created by Tomasz Kiljańczyk on 10/19/20 4:40 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/19/20 4:35 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.SpinnerItemSelectedListener


class SetlistEditorSongListFragment : Fragment() {

    private var searchView: TextInputLayout? = null
    private var categorySpinner: Spinner? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.text_view_filter_songs)
        categorySpinner = view.findViewById(R.id.spinner_category)

        SongsContext.setupSongListAdapter(showCheckBox = true)

        view.findViewById<RecyclerView>(R.id.recycler_view_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SongsContext.songsListAdapter
        }

        setupListeners()

        val categorySpinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        categorySpinner!!.apply {
            adapter = categorySpinnerAdapter
        }
    }

    private fun setupListeners() {
        searchView!!.editText!!.addTextChangedListener(InputTextChangeListener {
            SongsContext.filter(it, categorySpinner!!.selectedItem.toString())
        })

        categorySpinner!!.onItemSelectedListener =
            SpinnerItemSelectedListener { _, _ ->
                SongsContext.filter(
                    searchView!!.editText!!.editableText.toString(),
                    categorySpinner!!.selectedItem.toString()
                )
            }
    }
}
