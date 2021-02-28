/*
 * Created by Tomasz Kiljańczyk on 2/28/21 11:18 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/28/21 10:55 PM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.activities.SongControlsActivity
import pl.gunock.lyriccast.activities.SongEditorActivity
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.adapters.listeners.ClickAdapterListener
import pl.gunock.lyriccast.adapters.listeners.LongClickAdapterListener
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.SpinnerItemSelectedListener
import pl.gunock.lyriccast.models.SongItemModel
import kotlin.system.measureTimeMillis


class SongListFragment : Fragment() {
    private companion object {
        const val TAG = "SongListFragment"
    }

    private var castContext: CastContext? = null

    private lateinit var menu: Menu
    private lateinit var searchViewEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var songListRecyclerView: RecyclerView

    private var songItems: Set<SongItemModel> = setOf()
    private lateinit var songListAdapter: SongListAdapter
    private var selectionCount: Int = 0

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)

        val deleteActionItem = menu.findItem(R.id.action_delete)
        deleteActionItem.isVisible = false

        val editActionItem = menu.findItem(R.id.action_edit)
        editActionItem.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> deleteSelectedSongs()
            R.id.action_edit -> editSelectedSong()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        castContext = CastContext.getSharedInstance()

        val searchView: TextInputLayout = view.findViewById(R.id.text_view_filter_songs)
        searchViewEditText = searchView.editText!!

        categorySpinner = view.findViewById(R.id.spinner_category)
        songListRecyclerView = view.findViewById(R.id.recycler_view_songs)

        view.findViewById<SwitchCompat>(R.id.switch_selected_songs).visibility = View.GONE

        with(songListRecyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = null
        }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        setupCategorySpinner()
        setupSongList()
        resetSelection()

        searchViewEditText.setText("")
        categorySpinner.setSelection(0)
    }

    private fun setupListeners() {
        searchViewEditText.addTextChangedListener(InputTextChangeListener {
            filterSongs(
                searchViewEditText.editableText.toString(),
                category = categorySpinner.selectedItem.toString()
            )
        })

        categorySpinner.onItemSelectedListener = SpinnerItemSelectedListener { _, _ ->
            filterSongs(
                searchViewEditText.editableText.toString(),
                category = categorySpinner.selectedItem.toString()
            )
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("All") + SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categorySpinnerAdapter
    }

    private fun setupSongList() {
        songItems = SongsContext.getSongItems()

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val showAuthor = prefs.getBoolean("showAuthor", true)

        val onLongClickListener =
            LongClickAdapterListener { holder: SongListAdapter.SongViewHolder, position: Int, _ ->
                val item = songListAdapter.songItems[position]
                selectSong(item, holder)
                return@LongClickAdapterListener true
            }

        val onClickListener =
            ClickAdapterListener { holder: SongListAdapter.SongViewHolder, position: Int, _ ->
                val item: SongItemModel = songListAdapter.songItems[position]
                if (selectionCount == 0) {
                    pickSong(item)
                } else {
                    requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    selectSong(item, holder)
                }
            }

        songListAdapter = SongListAdapter(
            songItems.toMutableList(),
            showCheckBox = false,
            showAuthor = showAuthor,
            onLongClickListener = onLongClickListener,
            onClickListener = onClickListener
        )

        songListRecyclerView.adapter = songListAdapter
    }

    private fun filterSongs(title: String, category: String = "All") {
        Log.v(TAG, "filterSongs invoked")

        resetSelection()

        val normalizedTitle = title.normalize()

        val predicates: MutableList<(SongItemModel) -> Boolean> = mutableListOf()

        if (category != "All") {
            predicates.add { songItem -> songItem.category == category }
        }

        predicates.add { songItem ->
            songItem.title.normalize().contains(normalizedTitle, ignoreCase = true)
        }

        val duration = measureTimeMillis {
            songListAdapter.songItems = songItems.filter { songItem ->
                predicates.all { predicate -> predicate(songItem) }
            }.toMutableList()
        }
        Log.v(TAG, "Filtering took : ${duration}ms")

        songListAdapter.notifyDataSetChanged()
    }

    private fun pickSong(item: SongItemModel) {
        val songSections = SongsContext.getSongLyrics(item.title)!!.lyrics
        val songMetadata = SongsContext.getSongMetadata(item.title)!!
        val lyrics = songMetadata.presentation.map { sectionName -> songSections[sectionName]!! }

        val intent = Intent(requireContext(), SongControlsActivity::class.java)
        intent.putExtra("songTitle", item.title)
        intent.putExtra("lyrics", lyrics.toTypedArray())
        startActivity(intent)
    }

    private fun selectSong(item: SongItemModel, holder: SongListAdapter.SongViewHolder) {
        if (!item.isSelected) {
            selectionCount++
        } else {
            selectionCount--
        }

        var datasetChanged = false
        when (selectionCount) {
            0 -> {
                datasetChanged = true
                songListAdapter.showCheckBox = false

                val deleteActionItem = menu.findItem(R.id.action_delete)
                deleteActionItem.isVisible = false

                val editActionItem = menu.findItem(R.id.action_edit)
                editActionItem.isVisible = false
            }
            1 -> {
                datasetChanged = true
                songListAdapter.showCheckBox = true

                val deleteActionItem = menu.findItem(R.id.action_delete)
                deleteActionItem.isVisible = true

                val editActionItem = menu.findItem(R.id.action_edit)
                editActionItem.isVisible = true
            }
            2 -> {
                val deleteActionItem = menu.findItem(R.id.action_delete)
                deleteActionItem.isVisible = true

                val editActionItem = menu.findItem(R.id.action_edit)
                editActionItem.isVisible = false
            }
        }

        item.isSelected = !item.isSelected

        if (datasetChanged) {
            songListAdapter.notifyDataSetChanged()
        } else {
            holder.checkBox.isChecked = item.isSelected
        }
    }

    private fun editSelectedSong(): Boolean {
        val selectedSong = songListAdapter.songItems.first { songItem -> songItem.isSelected }

        val intent = Intent(requireContext(), SongEditorActivity::class.java)
        intent.putExtra("songTitle", selectedSong.title)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun deleteSelectedSongs(): Boolean {
        val selectedSongs = songListAdapter.songItems
            .filter { song -> song.isSelected }
            .map { song -> song.title }

        SongsContext.deleteSongs(selectedSongs)

        val remainingSongs = songListAdapter.songItems
            .filter { songItem -> !selectedSongs.contains(songItem.title) }
        songListAdapter.showCheckBox = false

        songListAdapter.songItems.clear()
        songListAdapter.songItems.addAll(remainingSongs)

        resetSelection()

        return true
    }

    private fun resetSelection() {
        songListAdapter.showCheckBox = false
        songListAdapter.notifyDataSetChanged()
        selectionCount = 0

        if (!this::menu.isInitialized) {
            return
        }

        val deleteActionItem = menu.findItem(R.id.action_delete)
        deleteActionItem.isVisible = false

        val editActionItem = menu.findItem(R.id.action_edit)
        editActionItem.isVisible = false
    }
}
