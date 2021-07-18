/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:19
 */

package pl.gunock.lyriccast.ui.setlist_editor

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
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import pl.gunock.lyriccast.databinding.FragmentSongsBinding
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import pl.gunock.lyriccast.datamodel.documents.SongDocument
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.extensions.hideKeyboard
import pl.gunock.lyriccast.ui.shared.adapters.CategorySpinnerAdapter
import pl.gunock.lyriccast.ui.shared.adapters.SongItemsAdapter
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker


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
        view.hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()

        mSongItemsAdapter!!.removeObservers()
        mSongItemsAdapter = null

        mDatabaseViewModel.close()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        requireView().hideKeyboard()
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
                view.hideKeyboard()
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
                if (mSongItemsAdapter == null) {
                    return@launch
                }

                mSongItemsAdapter!!.submitCollection(songs)
                withContext(Dispatchers.Default) {
                    mSongItemsAdapter!!.songItems.forEach { item ->
                        val previousValue = item.isSelected.value
                        val newValue = mArgs.presentation.contains(item.song.id.toString())

                        if (newValue != previousValue) {
                            item.isSelected.postValue(newValue)
                        }
                    }
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
