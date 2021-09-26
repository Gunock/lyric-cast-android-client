/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 17:19
 */

package pl.gunock.lyriccast.ui.setlist_editor

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.databinding.FragmentSongsBinding
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.shared.extensions.hideKeyboard
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.adapters.CategorySpinnerAdapter
import pl.gunock.lyriccast.ui.shared.adapters.SongItemsAdapter
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import javax.inject.Inject

@AndroidEntryPoint
class SetlistEditorSongsFragment : Fragment() {
    private companion object {
        const val TAG = "SetlistEditorSongsFg"
    }

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    private val mArgs: SetlistEditorSongsFragmentArgs by navArgs()

    private lateinit var mBinding: FragmentSongsBinding

    private var mSongItemsAdapter: SongItemsAdapter? = null
    private var mSelectedSongs: MutableSet<Song> = mutableSetOf()

    private var mSongsSubscription: Disposable? = null
    private var mCategoriesSubscription: Disposable? = null

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

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        mSongsSubscription = songsRepository.getAllSongs().subscribe { songs ->
            lifecycleScope.launch {
                if (mSongItemsAdapter == null) {
                    return@launch
                }

                val songItems = songs.map { SongItem(it) }
                mSongItemsAdapter!!.submitCollection(songItems)
                withContext(Dispatchers.Default) {
                    mSongItemsAdapter!!.songItems.forEach { item ->
                        val previousValue = item.isSelected.value
                        val newValue = mArgs.presentation.contains(item.song.id)

                        if (newValue != previousValue) {
                            item.isSelected.postValue(newValue)
                        }
                    }
                }
            }
        }

        mCategoriesSubscription = categoriesRepository.getAllCategories().subscribe { categories ->
            lifecycleScope.launch(Dispatchers.Main) {
                val categorySpinnerAdapter =
                    mBinding.spnCategory.adapter as CategorySpinnerAdapter
                categorySpinnerAdapter.submitCollection(categories)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        requireView().hideKeyboard()

        mSongsSubscription?.dispose()
        mSongsSubscription = null

        mCategoriesSubscription?.dispose()
        mCategoriesSubscription = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                updateSelectedSongs()

                val setlistSongIds = mArgs.presentation
                    .distinct()
                    .toMutableList()

                val selectedSongIds = mSelectedSongs.map { it.id }

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
                filterSongs(newText, (mBinding.spnCategory.selectedItem as Category).id)
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
                    (mBinding.spnCategory.selectedItem as Category).id
                )
            }
        }

        mBinding.swtSelectedSongs.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch(Dispatchers.Main) {
                filterSongs(
                    mBinding.edSongFilter.editableText.toString(),
                    (mBinding.spnCategory.selectedItem as Category).id,
                    isSelected = if (isChecked) true else null
                )
            }
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(requireContext())
        mBinding.spnCategory.adapter = categorySpinnerAdapter
    }

    private fun setupRecyclerView() {
        mBinding.rcvSongs.setHasFixedSize(true)
        mBinding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())

        val selectionTracker =
            SelectionTracker { _: BaseViewHolder, position: Int, _: Boolean ->
                val item: SongItem = mSongItemsAdapter!!.songItems[position]
                selectSong(item)
                return@SelectionTracker true
            }

        mSongItemsAdapter = SongItemsAdapter(
            requireContext(),
            showCheckBox = MutableLiveData(true),
            selectionTracker = selectionTracker
        )

        mBinding.rcvSongs.adapter = mSongItemsAdapter
    }

    private fun selectSong(item: SongItem) {
        item.isSelected.postValue(!item.isSelected.value!!)
    }

    private suspend fun filterSongs(
        title: String,
        categoryId: String,
        isSelected: Boolean? = null
    ) {
        Log.d(TAG, "filterSongs invoked")

        updateSelectedSongs()
        // TODO: Add filtering in viewModel
//        mSongItemsAdapter!!.filterItems(title, categoryId, isSelected)
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
