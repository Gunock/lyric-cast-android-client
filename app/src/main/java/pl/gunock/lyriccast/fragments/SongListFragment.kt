/*
 * Created by Tomasz Kiljańczyk on 10/20/20 10:55 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/20/20 10:00 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.textfield.TextInputLayout
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

    private var searchView: TextInputLayout? = null
    private var categorySpinner: Spinner? = null

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

        SongsContext.setupSongListAdapter()

        view.findViewById<Switch>(R.id.switch_selected_songs).visibility = View.GONE

        view.findViewById<RecyclerView>(R.id.recycler_view_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SongsContext.songsListAdapter
        }

        setupListeners(view)

        if (SongsContext.songList.isEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val songList: List<SongMetadataModel> = SongsContext.loadSongsMetadata()
                lifecycleScope.launch(Dispatchers.Main) {
                    SongsContext.fillSongsList(songList)
                    setupCategorySpinner()
                }
            }
        }
        if (SongsContext.categories.toList().isNotEmpty()) {
            setupCategorySpinner()
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner!!.apply {
            adapter = categorySpinnerAdapter
        }
    }

    private fun setupListeners(view: View) {
        view.findViewById<RecyclerView>(R.id.recycler_view_songs).addOnItemTouchListener(
            RecyclerItemClickListener(context) { _, position ->
                val songItem = SongsContext.songItemList[position]
                SongsContext.pickSong(songItem.title)
                findNavController().navigate(R.id.action_SongListFragment_to_ControlsFragment)
            })

        searchView!!.editText!!.addTextChangedListener(InputTextChangeListener {
            SongsContext.filterSongs(it, categorySpinner!!.selectedItem.toString())
        })

        categorySpinner!!.onItemSelectedListener = SpinnerItemSelectedListener { _, _ ->
            SongsContext.filterSongs(
                searchView!!.editText!!.editableText.toString(),
                categorySpinner!!.selectedItem.toString()
            )
        }
    }
}
