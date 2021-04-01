/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 2:42 AM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.SongControlsActivity
import pl.gunock.lyriccast.activities.SongEditorActivity
import pl.gunock.lyriccast.adapters.SongItemsAdapter
import pl.gunock.lyriccast.adapters.spinner.CategorySpinnerAdapter
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.LyricCastViewModel
import pl.gunock.lyriccast.datamodel.LyricCastViewModelFactory
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.helpers.KeyboardHelper
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.SongItem


class SongsFragment : Fragment() {
    private companion object {
        const val TAG = "SongsFragment"
    }

    private var castContext: CastContext? = null
    private lateinit var repository: LyricCastRepository
    private val lyricCastViewModel: LyricCastViewModel by viewModels {
        LyricCastViewModelFactory((requireActivity().application as LyricCastApplication).repository)
    }

    private lateinit var menu: Menu
    private lateinit var searchViewEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var songItemsRecyclerView: RecyclerView

    private lateinit var songItemsAdapter: SongItemsAdapter
    private lateinit var selectionTracker: SelectionTracker<SongItemsAdapter.ViewHolder>

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

        castContext = CastContext.getSharedInstance()

        val searchView: TextInputLayout = view.findViewById(R.id.tv_filter_songs)
        searchViewEditText = searchView.editText!!

        categorySpinner = view.findViewById(R.id.spn_category)
        songItemsRecyclerView = view.findViewById(R.id.rcv_songs)

        view.findViewById<SwitchCompat>(R.id.swt_selected_songs).visibility = View.GONE

        songItemsRecyclerView.setHasFixedSize(true)
        songItemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        setupCategorySpinner()
        setupSongs()
        resetSelection()

        searchViewEditText.setText("")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)

        showMenuActions(showDelete = false, showEdit = false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> deleteSelectedSongs()
            R.id.menu_edit -> editSelectedSong()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        searchViewEditText.addTextChangedListener(InputTextChangedListener {
            filterSongs(
                searchViewEditText.editableText.toString(),
                getSelectedCategoryId()
            )
        })

        searchViewEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view)
            }
        }

        categorySpinner.onItemSelectedListener = ItemSelectedSpinnerListener { _, _ ->
            filterSongs(
                searchViewEditText.editableText.toString(),
                getSelectedCategoryId()
            )
        }

    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(requireContext())
        categorySpinner.adapter = categorySpinnerAdapter

        lyricCastViewModel.allCategories.observe(requireActivity()) { categories ->
            categorySpinnerAdapter.submitCollection(categories)
            categorySpinner.setSelection(0)
        }
    }

    private fun setupSongs() {
        selectionTracker = SelectionTracker(songItemsRecyclerView, this::onSongClick)
        songItemsAdapter = SongItemsAdapter(
            requireContext(),
            selectionTracker = selectionTracker
        )
        songItemsRecyclerView.adapter = songItemsAdapter

        lyricCastViewModel.allSongs.observe(this, { songs ->
            songItemsAdapter.submitCollection(songs ?: return@observe)
        })
    }

    private fun onSongClick(
        holder: SongItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = songItemsAdapter.songItems[position]
        if (!isLongClick && selectionTracker.count == 0) {
            pickSong(item)
        } else {
            selectSong(item, holder)
        }
        return true
    }

    private fun filterSongs(
        title: String,
        categoryId: Long = Long.MIN_VALUE
    ) {
        Log.v(TAG, "filterSongs invoked")

        resetSelection()
        songItemsAdapter.filterItems(title, categoryId = categoryId)
    }

    private fun pickSong(item: SongItem) {
        val songWithLyrics = runBlocking { repository.getSongWithLyrics(item.song.id) }!!

        val lyricsTextMap = songWithLyrics.lyricsSectionsToTextMap()
        val lyrics: List<String> = songWithLyrics.crossRef
            .sorted()
            .map { lyricsTextMap[it.lyricsSectionId]!! }

        val intent = Intent(requireContext(), SongControlsActivity::class.java)
        intent.putExtra("lyrics", lyrics.toTypedArray())
        startActivity(intent)
    }

    private fun selectSong(item: SongItem, holder: SongItemsAdapter.ViewHolder) {
        item.isSelected = !item.isSelected
        holder.checkBox.isChecked = item.isSelected

        when (selectionTracker.countAfter) {
            0 -> {
                if (songItemsAdapter.showCheckBox.value!!) {
                    songItemsAdapter.showCheckBox.value = false
                }
                showMenuActions(showDelete = false, showEdit = false)
            }
            1 -> {
                if (!songItemsAdapter.showCheckBox.value!!) {
                    songItemsAdapter.showCheckBox.value = true
                }
                showMenuActions()
            }
            2 -> showMenuActions(showEdit = false)
        }
    }

    private fun editSelectedSong(): Boolean {
        val selectedItem = songItemsAdapter.songItems.first { songItem -> songItem.isSelected }
        Log.v(TAG, "Editing song : ${selectedItem.song}")
        val intent = Intent(requireContext(), SongEditorActivity::class.java)
        intent.putExtra("song", selectedItem.song)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun deleteSelectedSongs(): Boolean {
        val selectedSongs = songItemsAdapter.songItems
            .filter { item -> item.isSelected }
            .map { item -> item.song.id }

        lyricCastViewModel.deleteSongs(selectedSongs)

        resetSelection()

        return true
    }

    private fun resetSelection() {
        if (songItemsAdapter.showCheckBox.value!!) {
            songItemsAdapter.showCheckBox.value = false
        }

        showMenuActions(showDelete = false, showEdit = false)
    }

    private fun showMenuActions(showDelete: Boolean = true, showEdit: Boolean = true) {
        if (!this::menu.isInitialized) {
            return
        }

        menu.findItem(R.id.menu_delete).isVisible = showDelete
        menu.findItem(R.id.menu_edit).isVisible = showEdit
    }

    private fun getSelectedCategoryId(): Long {
        return ((categorySpinner.selectedItem ?: Category.ALL_CATEGORY) as Category).id
    }
}
