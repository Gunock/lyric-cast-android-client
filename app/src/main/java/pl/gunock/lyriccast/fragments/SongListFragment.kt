/*
 * Created by Tomasz Kiljańczyk on 2/25/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/25/21 9:57 PM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import pl.gunock.lyriccast.activities.SongEditorActivity
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.adapters.listeners.ClickAdapterListener
import pl.gunock.lyriccast.adapters.listeners.LongClickAdapterListener
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.SpinnerItemSelectedListener
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.models.SongMetadataModel


class SongListFragment : Fragment() {
    private companion object {
        const val TAG = "SongListFragment"
    }

    private var castContext: CastContext? = null

    private lateinit var menu: Menu
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

        loadSongs()
    }

    private fun setupListeners() {
        searchView.editText!!.addTextChangedListener(InputTextChangeListener {
            SongsContext.filterSongs(it, categorySpinner.selectedItem.toString())
        })

        categorySpinner.onItemSelectedListener = SpinnerItemSelectedListener { _, _ ->
            SongsContext.filterSongs(
                searchView.editText!!.editableText.toString(),
                categorySpinner.selectedItem.toString()
            )
        }
    }

    override fun onStart() {
        super.onStart()

        searchView.editText!!.setText("")
        categorySpinner.setSelection(0)

        setupSongList()
    }

    private fun loadSongs() {
        if (SongsContext.songMap.isEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val songList: List<SongMetadataModel> = SongsContext.loadSongsMetadata()
                CoroutineScope(Dispatchers.Main).launch {
                    SongsContext.fillSongsList(songList)
                    setupCategorySpinner()
                    setupSongList()
                    setupListeners()
                }
            }
        } else if (SongsContext.categories.toList().isNotEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                setupCategorySpinner()
                setupListeners()
            }
        }
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

        requireView().findViewById<RecyclerView>(R.id.recycler_view_songs)
            ?.adapter = songListAdapter
        SongsContext.songListAdapter = songListAdapter
    }

    private fun pickSong(item: SongItemModel) {
        val songSections = SongsContext.getSongLyrics(item.title).lyrics
        val songMetadata = SongsContext.getSongMetadata(item.title)
        val lyrics = songMetadata.presentation.map { songSections[it]!! }

        val action =
            SongListFragmentDirections.actionSongListFragmentToControlsFragment(
                songTitle = item.title,
                lyrics = lyrics.toTypedArray()
            )

        findNavController().navigate(action)
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

    private fun editSelectedSong(): Boolean {
        val selectedSong = songListAdapter.songs
            .first { it.isSelected }

        val intent = Intent(requireContext(), SongEditorActivity::class.java)
        intent.putExtra("songTitle", selectedSong.title)
        startActivity(intent)

        songListAdapter.showCheckBox = false
        songListAdapter.notifyDataSetChanged()
        selectionCount = 0

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
        songListAdapter.notifyDataSetChanged()

        selectionCount = 0

        return true
    }
}
