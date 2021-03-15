/*
 * Created by Tomasz Kiljańczyk on 3/15/21 3:53 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 3:50 AM
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.CategoriesContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.activities.SongControlsActivity
import pl.gunock.lyriccast.activities.SongEditorActivity
import pl.gunock.lyriccast.adapters.SongItemsAdapter
import pl.gunock.lyriccast.adapters.spinner.CategorySpinnerAdapter
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.Category
import pl.gunock.lyriccast.models.SongItem
import kotlin.system.measureTimeMillis


class SongsFragment : Fragment() {
    private companion object {
        const val TAG = "SongsFragment"
    }

    private var castContext: CastContext? = null

    private lateinit var menu: Menu
    private lateinit var searchViewEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var songItemsRecyclerView: RecyclerView

    private var songItems: Set<SongItem> = setOf()
    private lateinit var songItemsAdapter: SongItemsAdapter
    private lateinit var selectionTracker: SelectionTracker<SongItemsAdapter.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

        with(songItemsRecyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        setupCategorySpinner()
        setupSongs()
        resetSelection()

        searchViewEditText.setText("")
        categorySpinner.setSelection(0)
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
                category = categorySpinner.selectedItem as Category
            )
        })

        categorySpinner.onItemSelectedListener = ItemSelectedSpinnerListener { _, _ ->
            filterSongs(
                searchViewEditText.editableText.toString(),
                category = categorySpinner.selectedItem as Category
            )
        }
    }

    private fun setupCategorySpinner() {
        val categories = CategoriesContext.getCategories()

        val categorySpinnerAdapter = CategorySpinnerAdapter(
            requireContext(),
            setOf(Category("All")) + categories
        )
        categorySpinner.adapter = categorySpinnerAdapter
    }

    private fun setupSongs() {
        songItems = SongsContext.getSongItems()

        selectionTracker = SelectionTracker(songItemsRecyclerView, this::onSongClick)
        songItemsAdapter = SongItemsAdapter(
            requireContext(),
            songItems.toMutableList(),
            selectionTracker = selectionTracker
        )
        songItemsRecyclerView.adapter = songItemsAdapter
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

    private fun filterSongs(title: String, category: Category = Category("All")) {
        Log.v(TAG, "filterSongs invoked")

        resetSelection()

        val normalizedTitle = title.normalize()

        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (category.name != "All") {
            predicates.add { songItem -> songItem.category?.id == category.id }
        }

        predicates.add { songItem ->
            songItem.title.normalize().contains(normalizedTitle, ignoreCase = true)
        }

        val duration = measureTimeMillis {
            songItemsAdapter.songItems = songItems.filter { songItem ->
                predicates.all { predicate -> predicate(songItem) }
            }.toMutableList()
        }
        Log.v(TAG, "Filtering took : ${duration}ms")

        songItemsAdapter.notifyDataSetChanged()
    }

    private fun pickSong(item: SongItem) {
        val songSections = SongsContext.getSongLyrics(item.id)!!.lyrics
        val songMetadata = SongsContext.getSongMetadata(item.id)!!
        val lyrics = songMetadata.presentation.map { sectionName -> songSections[sectionName]!! }

        val intent = Intent(requireContext(), SongControlsActivity::class.java)
        intent.putExtra("songTitle", item.title)
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
        val selectedSong = songItemsAdapter.songItems.first { songItem -> songItem.isSelected }

        val intent = Intent(requireContext(), SongEditorActivity::class.java)
        intent.putExtra("songId", selectedSong.id)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun deleteSelectedSongs(): Boolean {
        val selectedSongs = songItemsAdapter.songItems
            .filter { song -> song.isSelected }
            .map { song -> song.id }

        SongsContext.deleteSongs(selectedSongs)

        val remainingSongs = songItemsAdapter.songItems
            .filter { songItem -> !selectedSongs.contains(songItem.id) }

        songItemsAdapter.songItems.clear()
        songItemsAdapter.songItems.addAll(remainingSongs)

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
}
