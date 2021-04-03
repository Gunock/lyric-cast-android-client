/*
 * Created by Tomasz Kiljanczyk on 4/3/21 9:09 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/3/21 9:08 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.SongItemsAdapter
import pl.gunock.lyriccast.adapters.spinner.CategorySpinnerAdapter
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.LyricCastViewModel
import pl.gunock.lyriccast.datamodel.LyricCastViewModelFactory
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.SetlistSongCrossRef
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.helpers.KeyboardHelper
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.SongItem


class SetlistEditorSongsFragment : Fragment() {
    private companion object {
        const val TAG = "SetlistEditorSongsFg"
    }

    private val args: SetlistEditorSongsFragmentArgs by navArgs()
    private lateinit var repository: LyricCastRepository
    private val lyricCastViewModel: LyricCastViewModel by viewModels {
        LyricCastViewModelFactory(
            requireContext(),
            (requireActivity().application as LyricCastApplication).repository
        )
    }

    private lateinit var songTitleInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var selectedSongsSwitch: SwitchCompat

    private lateinit var songItemsAdapter: SongItemsAdapter
    private lateinit var selectedSongs: MutableSet<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        repository = (requireActivity().application as LyricCastApplication).repository
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedSongs = args.setlistWithSongs.songs.toMutableSet()

        val searchView: TextInputLayout = view.findViewById(R.id.tv_filter_songs)
        songTitleInput = searchView.editText!!
        categorySpinner = view.findViewById(R.id.spn_category)
        selectedSongsSwitch = view.findViewById(R.id.swt_selected_songs)

        setupSongs(view)
        setupCategorySpinner()
        setupListeners()
        KeyboardHelper.hideKeyboard(view)
    }

    override fun onPause() {
        super.onPause()
        KeyboardHelper.hideKeyboard(requireView())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                updateSelectedSongs()

                val setlistSongs = args.setlistWithSongs.songs
                    .toMutableList()

                val removedSongs =
                    setlistSongs.filter { song -> !this.selectedSongs.contains(song) }
                val addedSongs =
                    this.selectedSongs.filter { song -> !setlistSongs.contains(song) }

                setlistSongs.removeAll(removedSongs)
                setlistSongs.addAll(addedSongs)

                val removedSongIds = removedSongs.map { song -> song.id }
                var setlistSongCrossRefs = args.setlistWithSongs.setlistSongCrossRefs
                    .toMutableList()

                setlistSongCrossRefs = setlistSongCrossRefs.filter { crossRef ->
                    !removedSongIds.contains(crossRef.songId)
                }.toMutableList()

                val setlist = args.setlistWithSongs.setlist
                addedSongs.forEachIndexed { index, song ->
                    val setlistSongCrossRef = SetlistSongCrossRef(setlist.id, song.id, index)
                    setlistSongCrossRefs.add(setlistSongCrossRef)
                }

                val setlistWithSongs = SetlistWithSongs(setlist, setlistSongs, setlistSongCrossRefs)
                val action = SetlistEditorSongsFragmentDirections
                    .actionSetlistEditorSongsToSetlistEditor(setlistWithSongs = setlistWithSongs)

                findNavController().navigate(action)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        songTitleInput.addTextChangedListener(InputTextChangedListener { newText ->
            filterSongs(newText, (categorySpinner.selectedItem as Category).id)
        })

        songTitleInput.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view)
            }
        }

        categorySpinner.onItemSelectedListener =
            ItemSelectedSpinnerListener { _, _ ->
                filterSongs(
                    songTitleInput.editableText.toString(),
                    (categorySpinner.selectedItem as Category).id
                )
            }

        selectedSongsSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterSongs(
                songTitleInput.editableText.toString(),
                (categorySpinner.selectedItem as Category).id,
                isSelected = if (isChecked) true else null
            )
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(requireContext())
        categorySpinner.adapter = categorySpinnerAdapter

        lyricCastViewModel.allCategories.observe(requireActivity()) { categories ->
            categorySpinnerAdapter.submitCollection(categories)
        }
    }

    private fun setupSongs(view: View) {
        val songsRecyclerView: RecyclerView = view.findViewById(R.id.rcv_songs)
        val selectionTracker =
            SelectionTracker(songsRecyclerView) { holder: SongItemsAdapter.ViewHolder, position: Int, _: Boolean ->
                val item: SongItem = songItemsAdapter.songItems[position]
                selectSong(item)
                return@SelectionTracker true
            }

        songItemsAdapter = SongItemsAdapter(
            requireContext(),
            showCheckBox = MutableLiveData(true),
            selectionTracker = selectionTracker
        )

        with(songsRecyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songItemsAdapter
        }

        lyricCastViewModel.allSongs.observe(requireActivity()) { songs ->
            songItemsAdapter.submitCollection(songs ?: return@observe)
            songItemsAdapter.songItems.forEach { item ->
                item.isSelected.value = selectedSongs.contains(item.song)
            }
        }
    }

    private fun selectSong(item: SongItem) {
        item.isSelected.value = !item.isSelected.value!!
    }

    private fun filterSongs(
        title: String,
        categoryId: Long = Long.MIN_VALUE,
        isSelected: Boolean? = null
    ) {
        Log.d(TAG, "filterSongs invoked")

        updateSelectedSongs()
        songItemsAdapter.filterItems(title, categoryId, isSelected)
    }

    private fun updateSelectedSongs() {
        for (item in songItemsAdapter.songItems) {
            if (item.isSelected.value!!) {
                selectedSongs.add(item.song)
            } else {
                selectedSongs.remove(item.song)
            }
        }
    }
}
