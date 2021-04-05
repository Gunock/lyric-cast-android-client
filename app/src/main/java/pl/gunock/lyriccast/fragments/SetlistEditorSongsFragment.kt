/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:11 PM
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
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.DatabaseViewModelFactory
import pl.gunock.lyriccast.datamodel.LyricCastRepository
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

    private val mArgs: SetlistEditorSongsFragmentArgs by navArgs()
    private lateinit var mRepository: LyricCastRepository
    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModelFactory(
            requireContext(),
            (requireActivity().application as LyricCastApplication).repository
        )
    }

    private lateinit var mSongTitleInput: EditText
    private lateinit var mCategorySpinner: Spinner
    private lateinit var mSelectedSongsSwitch: SwitchCompat

    private lateinit var mSongItemsAdapter: SongItemsAdapter
    private lateinit var mSelectedSongs: MutableSet<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mRepository = (requireActivity().application as LyricCastApplication).repository
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSelectedSongs = mArgs.setlistWithSongs.songs.toMutableSet()

        val searchView: TextInputLayout = view.findViewById(R.id.tv_filter_songs)
        mSongTitleInput = searchView.editText!!
        mCategorySpinner = view.findViewById(R.id.spn_category)
        mSelectedSongsSwitch = view.findViewById(R.id.swt_selected_songs)

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

                val setlistSongs = mArgs.setlistWithSongs.songs
                    .toMutableList()

                val removedSongs =
                    setlistSongs.filter { song -> !this.mSelectedSongs.contains(song) }
                val addedSongs =
                    this.mSelectedSongs.filter { song -> !setlistSongs.contains(song) }

                setlistSongs.removeAll(removedSongs)
                setlistSongs.addAll(addedSongs)

                val removedSongIds = removedSongs.map { song -> song.id }
                var setlistSongCrossRefs = mArgs.setlistWithSongs.setlistSongCrossRefs
                    .toMutableList()

                setlistSongCrossRefs = setlistSongCrossRefs.filter { crossRef ->
                    !removedSongIds.contains(crossRef.songId)
                }.toMutableList()

                val setlist = mArgs.setlistWithSongs.setlist
                addedSongs.forEachIndexed { index, song ->
                    val setlistSongCrossRef = SetlistSongCrossRef(null, setlist.id, song.id, index)
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
        mSongTitleInput.addTextChangedListener(InputTextChangedListener { newText ->
            filterSongs(newText, (mCategorySpinner.selectedItem as Category).id)
        })

        mSongTitleInput.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view)
            }
        }

        mCategorySpinner.onItemSelectedListener =
            ItemSelectedSpinnerListener { _, _ ->
                filterSongs(
                    mSongTitleInput.editableText.toString(),
                    (mCategorySpinner.selectedItem as Category).id
                )
            }

        mSelectedSongsSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterSongs(
                mSongTitleInput.editableText.toString(),
                (mCategorySpinner.selectedItem as Category).id,
                isSelected = if (isChecked) true else null
            )
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(requireContext())
        mCategorySpinner.adapter = categorySpinnerAdapter

        mDatabaseViewModel.allCategories.observe(requireActivity()) { categories ->
            categorySpinnerAdapter.submitCollection(categories)
        }
    }

    private fun setupSongs(view: View) {
        val songsRecyclerView: RecyclerView = view.findViewById(R.id.rcv_songs)
        songsRecyclerView.setHasFixedSize(true)
        songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val selectionTracker =
            SelectionTracker(songsRecyclerView) { _: SongItemsAdapter.ViewHolder, position: Int, _: Boolean ->
                val item: SongItem = mSongItemsAdapter.songItems[position]
                selectSong(item)
                return@SelectionTracker true
            }

        mSongItemsAdapter = SongItemsAdapter(
            requireContext(),
            showCheckBox = MutableLiveData(true),
            mSelectionTracker = selectionTracker
        )

        mDatabaseViewModel.allSongs.observe(requireActivity()) { songs ->
            mSongItemsAdapter.submitCollection(songs ?: return@observe)
            mSongItemsAdapter.songItems.forEach { item ->
                item.isSelected.value = mSelectedSongs.contains(item.song)
            }
        }

        songsRecyclerView.adapter = mSongItemsAdapter
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
        mSongItemsAdapter.filterItems(title, categoryId, isSelected)
    }

    private fun updateSelectedSongs() {
        for (item in mSongItemsAdapter.songItems) {
            if (item.isSelected.value!!) {
                mSelectedSongs.add(item.song)
            } else {
                mSelectedSongs.remove(item.song)
            }
        }
    }
}
