/*
 * Created by Tomasz Kiljańczyk on 2/27/21 8:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 8:36 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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

    private val args: SetlistEditorFragmentArgs by navArgs()

    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var setlistNameInput: TextView
    private var setlistSongs: List<SongItemModel> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setlist_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val setlistName: String? = requireActivity().intent.getStringExtra("setlistName")

        setlistNameInput = view.findViewById(R.id.text_input_setlist_name)

        if (args.selectedSongs != null) {
            setlistSongs = SongsContext.getSongItems()
                .filter { songItem -> args.selectedSongs!!.contains(songItem.title) }

            setlistNameInput.text = args.setlistName
        } else if (setlistName != null) {
            setlistNameInput.text = setlistName

            val setlist = SetlistsContext.getSetlist(setlistName)

            setlistSongs = SongsContext.getSongItems()
                .filter { songItem -> setlist.songTitles.contains(songItem.title) }
        }

        songsRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupListeners(view)
    }

    override fun onResume() {
        super.onResume()

        songsRecyclerView.adapter = SongListAdapter(setlistSongs.toMutableList())
    }

    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.button_pick_setlist_songs).setOnClickListener {
            KeyboardHelper.hideKeyboard(view)

            val action = SetlistEditorFragmentDirections
                .actionSetlistEditorFragmentToSetlistEditorSongListFragment(
                    selectedSongs = setlistSongs.map { songItem -> songItem.title }.toTypedArray(),
                    setlistName = setlistNameInput.text.toString()
                )

            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.button_save_setlist).setOnClickListener {
            val setlist = SetlistModel()
            setlist.name = setlistNameInput.text.toString()

            if (setlistSongs.isEmpty()) {
                val toast = Toast.makeText(
                    requireContext(),
                    "Empty setlists are not allowed!",
                    Toast.LENGTH_SHORT
                )
                toast.show()
                return@setOnClickListener
            }

            setlist.songTitles = setlistSongs.map { songItem -> songItem.title }

            SetlistsContext.saveSetlist(setlist)

            requireActivity().finish()
        }
    }
}
