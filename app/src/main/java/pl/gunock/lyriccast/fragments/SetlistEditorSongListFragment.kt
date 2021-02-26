/*
 * Created by Tomasz Kiljańczyk on 2/26/21 9:36 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/26/21 9:36 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.SpinnerItemSelectedListener
import pl.gunock.lyriccast.models.SongItemModel
import kotlin.system.measureTimeMillis


class SetlistEditorSongListFragment : Fragment() {
    private companion object {
        const val TAG = "SetlistEditorSongListFg"
    }

    private val args: SetlistEditorSongListFragmentArgs by navArgs()

    private lateinit var searchView: TextInputLayout
    private lateinit var categorySpinner: Spinner
    private lateinit var selectedSongsSwitch: SwitchCompat

    private lateinit var songListAdapter: SongListAdapter

    private lateinit var selectedSongTitles: MutableSet<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedSongTitles = args.selectedSongs.toMutableSet()

        searchView = view.findViewById(R.id.text_view_filter_songs)
        categorySpinner = view.findViewById(R.id.spinner_category)
        selectedSongsSwitch = view.findViewById(R.id.switch_selected_songs)

        songListAdapter = SongListAdapter(SongsContext.songItemList, showCheckBox = true)
        view.findViewById<RecyclerView>(R.id.recycler_view_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songListAdapter
        }

        setupListeners()

        val categorySpinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        categorySpinner.apply {
            adapter = categorySpinnerAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                updateSelectedSongs()

                val action = SetlistEditorSongListFragmentDirections
                    .actionSetlistEditorSongListFragmentToSetlistEditorFragment(
                        selectedSongs = selectedSongTitles.toTypedArray(),
                        setlistName = args.setlistName
                    )

                findNavController().navigate(action)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        searchView.editText!!.addTextChangedListener(InputTextChangeListener {
            filterSongs(it, categorySpinner.selectedItem.toString())
        })

        categorySpinner.onItemSelectedListener =
            SpinnerItemSelectedListener { _, _ ->
                filterSongs(
                    searchView.editText!!.editableText.toString(),
                    categorySpinner.selectedItem.toString()
                )
            }

        selectedSongsSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterSongs(
                searchView.editText!!.editableText.toString(),
                categorySpinner.selectedItem.toString(),
                isSelected = if (isChecked) true else null
            )
        }
    }

    private fun filterSongs(title: String, category: String = "All", isSelected: Boolean? = null) {
        Log.d(TAG, "filterSongs invoked")

        updateSelectedSongs()

        val predicate = if (isSelected == null) { song: SongItemModel ->
            val titleCondition = song.title.normalize()
                .contains(title.normalize(), ignoreCase = true)
            val categoryCondition = (category == "All" || song.category == category)

            titleCondition && categoryCondition
        } else { song: SongItemModel ->
            if (song.isSelected != isSelected) {
                false
            } else {
                val titleCondition = song.title.normalize()
                    .contains(title.normalize(), ignoreCase = true)
                val categoryCondition = (category == "All" || song.category == category)

                titleCondition && categoryCondition
            }
        }

        val duration = measureTimeMillis {
            songListAdapter.songs = SongsContext.songItemList.filter(predicate).toMutableList()
        }
        Log.d(TAG, "Filtering took : ${duration}ms")

        songListAdapter.notifyDataSetChanged()
    }

    private fun updateSelectedSongs() {
        for (songItem in songListAdapter.songs) {
            if (songItem.isSelected) {
                selectedSongTitles.add(songItem.title)
            } else {
                selectedSongTitles.remove(songItem.title)
            }
        }
    }
}
