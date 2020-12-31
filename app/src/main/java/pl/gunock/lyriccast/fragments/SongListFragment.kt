/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 3:43 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.RecyclerItemClickListener
import pl.gunock.lyriccast.listeners.SpinnerItemSelectedListener
import pl.gunock.lyriccast.models.SongMetadataModel


class SongListFragment : Fragment() {

    private var castContext: CastContext? = null

    private lateinit var searchView: TextInputLayout
    private lateinit var categorySpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        castContext = CastContext.getSharedInstance()

        searchView = view.findViewById(R.id.text_view_filter_songs)
        categorySpinner = view.findViewById(R.id.spinner_category)

        view.findViewById<SwitchCompat>(R.id.switch_selected_songs).visibility = View.GONE

        view.findViewById<RecyclerView>(R.id.recycler_view_songs).run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = null
        }

        loadSongs()
    }

    private fun setupListeners(view: View) {
        view.findViewById<RecyclerView>(R.id.recycler_view_songs).addOnItemTouchListener(
            RecyclerItemClickListener(context) { _, position ->
                val songItem = SongsContext.songsListAdapter!!.songs[position]
                SongsContext.pickSong(songItem.title)
                findNavController().navigate(R.id.action_SongListFragment_to_ControlsFragment)
            })

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
                    setupListeners(requireView())
                }
            }
        }
        else if (SongsContext.categories.toList().isNotEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                setupCategorySpinner()
                setupListeners(requireView())
            }
        }
    }

    private fun setupSongList(){
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val showAuthor = prefs.getBoolean("showAuthor", true)

        SongsContext.setupSongListAdapter(showAuthor = showAuthor)
        view?.findViewById<RecyclerView>(R.id.recycler_view_songs)?.adapter =
            SongsContext.songsListAdapter
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
}
