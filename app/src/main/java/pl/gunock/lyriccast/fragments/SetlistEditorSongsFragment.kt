/*
 * Created by Tomasz Kiljanczyk on 15/05/2021, 15:20
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 15/05/2021, 14:53
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import pl.gunock.lyriccast.adapters.SongItemsAdapter
import pl.gunock.lyriccast.adapters.spinner.CategorySpinnerAdapter
import pl.gunock.lyriccast.databinding.FragmentSongsBinding
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import pl.gunock.lyriccast.datamodel.documents.SongDocument
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
    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.Factory(requireContext().resources)
    }
    private lateinit var mBinding: FragmentSongsBinding

    private var mSongItemsAdapter: SongItemsAdapter? = null
    private var mSelectedSongs: MutableSet<SongDocument> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSongsBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCategorySpinner()
        setupListeners()
        KeyboardHelper.hideKeyboard(view)
    }

    override fun onDestroy() {
        mSongItemsAdapter!!.removeObservers()
        mSongItemsAdapter = null

        mDatabaseViewModel.close()

        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        KeyboardHelper.hideKeyboard(requireView())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                updateSelectedSongs()

                val setlistSongIds = mArgs.presentation
                    .distinct()
                    .toMutableList()

                val selectedSongIds = mSelectedSongs.map { it.id.toString() }

                val removedSongIds =
                    setlistSongIds.filter { songId -> !selectedSongIds.contains(songId) }
                val addedSongIds =
                    selectedSongIds.filter { songId -> !setlistSongIds.contains(songId) }

                setlistSongIds.removeAll(removedSongIds)
                setlistSongIds.addAll(addedSongIds)

                val presentation: MutableList<String> = mArgs.presentation.toMutableList()
                presentation.removeAll { songId ->
                    !setlistSongIds.contains(songId)
                }

                presentation.addAll(addedSongIds)

                val action = SetlistEditorSongsFragmentDirections
                    .actionSetlistEditorSongsToSetlistEditor(
                        setlistId = mArgs.setlistId,
                        presentation = presentation.toTypedArray(),
                        setlistName = mArgs.setlistName
                    )

                findNavController().navigate(action)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        mBinding.edSongFilter.addTextChangedListener(InputTextChangedListener { newText ->
            lifecycleScope.launch(Dispatchers.Main) {
                filterSongs(newText, (mBinding.spnCategory.selectedItem as CategoryDocument).id)
            }
        })

        mBinding.edSongFilter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view)
            }
        }

        mBinding.spnCategory.onItemSelectedListener = ItemSelectedSpinnerListener { _, _ ->
            lifecycleScope.launch(Dispatchers.Main) {
                filterSongs(
                    mBinding.edSongFilter.editableText.toString(),
                    (mBinding.spnCategory.selectedItem as CategoryDocument).id
                )
            }
        }

        mBinding.swtSelectedSongs.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch(Dispatchers.Main) {
                filterSongs(
                    mBinding.edSongFilter.editableText.toString(),
                    (mBinding.spnCategory.selectedItem as CategoryDocument).id,
                    isSelected = if (isChecked) true else null
                )
            }
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(requireContext())

        mBinding.spnCategory.adapter = categorySpinnerAdapter

        mDatabaseViewModel.allCategories.addChangeListener { categories ->
            lifecycleScope.launch(Dispatchers.Main) {
                categorySpinnerAdapter.submitCollection(categories)
            }
        }
    }

    private fun setupRecyclerView() {
        mBinding.rcvSongs.setHasFixedSize(true)
        mBinding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())

        val selectionTracker =
            SelectionTracker { _: SongItemsAdapter.ViewHolder, position: Int, _: Boolean ->
                val item: SongItem = mSongItemsAdapter!!.songItems[position]
                selectSong(item)
                return@SelectionTracker true
            }

        mSongItemsAdapter = SongItemsAdapter(
            requireContext(),
            showCheckBox = MutableLiveData(true),
            mSelectionTracker = selectionTracker
        )

        mDatabaseViewModel.allSongs.addChangeListener { songs ->
            lifecycleScope.launch(Dispatchers.Main) {
                mSongItemsAdapter!!.submitCollection(songs)
                mSongItemsAdapter!!.songItems.forEach { item ->
                    item.isSelected.value = mArgs.presentation.contains(item.song.id.toString())
                }
            }
        }

        mBinding.rcvSongs.adapter = mSongItemsAdapter
    }

    private fun selectSong(item: SongItem) {
        item.isSelected.value = !item.isSelected.value!!
    }

    private suspend fun filterSongs(
        title: String,
        categoryId: ObjectId,
        isSelected: Boolean? = null
    ) {
        Log.d(TAG, "filterSongs invoked")

        updateSelectedSongs()
        mSongItemsAdapter!!.filterItems(title, categoryId, isSelected)
    }

    private fun updateSelectedSongs() {
        for (item in mSongItemsAdapter!!.songItems) {
            if (item.isSelected.value!!) {
                mSelectedSongs.add(item.song)
            } else {
                mSelectedSongs.remove(item.song)
            }
        }
    }
}
