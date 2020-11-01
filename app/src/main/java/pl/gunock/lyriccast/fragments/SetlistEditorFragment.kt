/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 11:40 AM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.SongListAdapter
import pl.gunock.lyriccast.models.SetlistModel
import pl.gunock.lyriccast.models.SongItemModel
import pl.gunock.lyriccast.utils.KeyboardHelper


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SetlistEditorFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setlist_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<RecyclerView>(R.id.recycler_view_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter =
                SongListAdapter(SongsContext.songItemList.filter { it.selected }.toMutableList())
        }

        setupListeners(view)
    }

    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.button_pick_setlist_songs).setOnClickListener {
            KeyboardHelper.hideKeyboard(view)
            findNavController().navigate(R.id.action_SetlistEditorFragment_to_SetlistEditorSongListFragment)
        }

        view.findViewById<Button>(R.id.button_save_setlist).setOnClickListener {
            val setlist = SetlistModel()
            setlist.name = view.findViewById<TextView>(R.id.text_input_setlist_name).text.toString()

            val selectedSongs: List<SongItemModel> =
                SongsContext.songItemList.filter { it.selected }
            setlist.songTitles = List(selectedSongs.size) {
                selectedSongs[it].title
            }

            SetlistsContext.setlistList.add(setlist)
            SetlistsContext.saveSetlist(setlist)

            activity?.finish()
        }
    }
}
