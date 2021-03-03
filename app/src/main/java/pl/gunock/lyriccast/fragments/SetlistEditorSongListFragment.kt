/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:02 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
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
import pl.gunock.lyriccast.adapters.SongItemsAdapter
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.models.SongItem
import kotlin.system.measureTimeMillis


class SetlistEditorSongListFragment : Fragment() {
    private companion object {
        const val TAG = "SetlistEditorSongListFg"
    }

    private val args: SetlistEditorSongListFragmentArgs by navArgs()

    private lateinit var searchViewEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var selectedSongsSwitch: SwitchCompat

    private lateinit var songItemsAdapter: SongItemsAdapter

    private lateinit var selectedSongTitles: MutableSet<String>

    private var songItems: Set<SongItem> = setOf()

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

        val searchView: TextInputLayout = view.findViewById(R.id.text_view_filter_songs)
        searchViewEditText = searchView.editText!!
        categorySpinner = view.findViewById(R.id.spinner_category)
        selectedSongsSwitch = view.findViewById(R.id.switch_selected_songs)

        setupSongList(view)
        setupListeners()

        val categorySpinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("All") + SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        categorySpinner.adapter = categorySpinnerAdapter
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
        searchViewEditText.addTextChangedListener(InputTextChangedListener { newText ->
            filterSongs(newText, categorySpinner.selectedItem.toString())
        })

        categorySpinner.onItemSelectedListener =
            ItemSelectedSpinnerListener { _, _ ->
                filterSongs(
                    searchViewEditText.editableText.toString(),
                    categorySpinner.selectedItem.toString()
                )
            }

        selectedSongsSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterSongs(
                searchViewEditText.editableText.toString(),
                categorySpinner.selectedItem.toString(),
                isSelected = if (isChecked) true else null
            )
        }
    }

    private fun setupSongList(view: View) {
        songItems = SongsContext.getSongItems()
        songItems.forEach { songItem ->
            songItem.isSelected = selectedSongTitles.contains(songItem.title)
        }

        songItemsAdapter = SongItemsAdapter(songItems.toMutableList(), showCheckBox = true)
        with(view.findViewById<RecyclerView>(R.id.recycler_view_songs)) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songItemsAdapter
        }
    }

    private fun filterSongs(title: String, category: String = "All", isSelected: Boolean? = null) {
        Log.d(TAG, "filterSongs invoked")

        updateSelectedSongs()

        val normalizedTitle = title.normalize()

        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (isSelected != null) {
            predicates.add { songItem -> songItem.isSelected }
        }

        if (category != "All") {
            predicates.add { songItem -> songItem.category == category }
        }

        predicates.add { songItem ->
            songItem.title.normalize().contains(normalizedTitle, ignoreCase = true)
        }

        val duration = measureTimeMillis {
            songItemsAdapter.songItems = songItems.filter { songItem ->
                predicates.all { predicate -> predicate(songItem) }
            }.toMutableList()
        }
        Log.d(TAG, "Filtering took : ${duration}ms")

        songItemsAdapter.notifyDataSetChanged()
    }

    private fun updateSelectedSongs() {
        for (songItem in songItemsAdapter.songItems) {
            if (songItem.isSelected) {
                selectedSongTitles.add(songItem.title)
            } else {
                selectedSongTitles.remove(songItem.title)
            }
        }
    }
}
