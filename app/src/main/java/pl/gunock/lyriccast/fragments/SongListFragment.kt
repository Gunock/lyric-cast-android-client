/*
 * Created by Tomasz Kiljańczyk on 2/27/21 2:30 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 2:28 AM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private var menu: Menu? = null
    private lateinit var searchView: TextInputLayout
    private lateinit var categorySpinner: Spinner
    private lateinit var songListRecyclerView: RecyclerView

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

        searchView = view.findViewById(R.id.text_view_filter_songs)
        categorySpinner = view.findViewById(R.id.spinner_category)
        songListRecyclerView = view.findViewById(R.id.recycler_view_songs)

        view.findViewById<SwitchCompat>(R.id.switch_selected_songs).visibility = View.GONE

        songListRecyclerView.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = null
        }

        CoroutineScope(Dispatchers.Main).launch {
            setupCategorySpinner()
            setupSongList()
            setupListeners()
        }
    }

    override fun onStart() {
        super.onStart()

        searchView.editText!!.setText("")
        categorySpinner.setSelection(0)

        setupSongList()
    }

    override fun onResume() {
        super.onResume()

        resetSelection()
    }

    private fun setupListeners() {
        searchView.editText!!.addTextChangedListener(InputTextChangeListener {
            filterSongs(it, categorySpinner.selectedItem.toString())
        })

        categorySpinner.onItemSelectedListener = SpinnerItemSelectedListener { _, _ ->
            filterSongs(
                searchView.editText!!.editableText.toString(),
                categorySpinner.selectedItem.toString()
            )
        }
    }

    private fun filterSongs(title: String, category: String = "All", isSelected: Boolean? = null) {
        Log.d(TAG, "filterSongs invoked")

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

    private fun setupSongList() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val showAuthor = prefs.getBoolean("showAuthor", true)

        SongsContext.songItemList.clear()
        for (i in SongsContext.songMap.values.indices) {
            SongsContext.songItemList.add(SongItemModel(SongsContext.songMap.values.elementAt(i)))
        }

        val onLongClickListener =
            LongClickAdapterListener { holder: SongListAdapter.SongViewHolder, position: Int, _ ->
                val item = songListAdapter.songs[position]
                selectSong(item, holder)
                return@LongClickAdapterListener true
            }

        val onClickListener =
            ClickAdapterListener { holder: SongListAdapter.SongViewHolder, position: Int, _ ->
                val item: SongItemModel = songListAdapter.songs[position]
                if (selectionCount == 0) {
                    pickSong(item)
                } else {
                    requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    selectSong(item, holder)
                }
            }

        songListAdapter = SongListAdapter(
            SongsContext.songItemList,
            showCheckBox = false,
            showAuthor = showAuthor,
            onLongClickListener = onLongClickListener,
            onClickListener = onClickListener
        )

        songListRecyclerView.adapter = songListAdapter
    }

    private fun setupCategorySpinner() {
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

    private fun pickSong(item: SongItemModel) {
        val songSections = SongsContext.getSongLyrics(item.title).lyrics
        val songMetadata = SongsContext.getSongMetadata(item.title)
        val lyrics = songMetadata.presentation.map { songSections[it]!! }

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

                val deleteActionItem = menu!!.findItem(R.id.action_delete)
                deleteActionItem.isVisible = false

                val editActionItem = menu!!.findItem(R.id.action_edit)
                editActionItem.isVisible = false
            }
            1 -> {
                datasetChanged = true
                songListAdapter.showCheckBox = true

                val deleteActionItem = menu!!.findItem(R.id.action_delete)
                deleteActionItem.isVisible = true

                val editActionItem = menu!!.findItem(R.id.action_edit)
                editActionItem.isVisible = true
            }
            2 -> {
                val deleteActionItem = menu!!.findItem(R.id.action_delete)
                deleteActionItem.isVisible = true

                val editActionItem = menu!!.findItem(R.id.action_edit)
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
        val selectedSong = songListAdapter.songs.first { it.isSelected }

        val intent = Intent(requireContext(), SongEditorActivity::class.java)
        intent.putExtra("songTitle", selectedSong.title)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun deleteSelectedSongs(): Boolean {
        val selectedSongs = songListAdapter.songs
            .filter { it.isSelected }
            .map { it.title }

        SongsContext.deleteSongs(selectedSongs)

        val remainingSongs = songListAdapter.songs.filter { !selectedSongs.contains(it.title) }
        songListAdapter.showCheckBox = false

        songListAdapter.songs.clear()
        songListAdapter.songs.addAll(remainingSongs)

        resetSelection()

        return true
    }

    private fun resetSelection() {
        songListAdapter.showCheckBox = false
        songListAdapter.notifyDataSetChanged()
        selectionCount = 0

        if (menu == null) {
            return
        }

        val deleteActionItem = menu!!.findItem(R.id.action_delete)
        deleteActionItem.isVisible = false

        val editActionItem = menu!!.findItem(R.id.action_edit)
        editActionItem.isVisible = false
    }
}
