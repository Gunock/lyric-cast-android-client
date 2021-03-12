/*
 * Created by Tomasz Kiljańczyk on 3/12/21 4:03 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/12/21 4:03 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.SongItemsAdapter
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.models.SongItem


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SetlistEditorFragment : Fragment() {

    private val args: SetlistEditorFragmentArgs by navArgs()
    private val setlistNameTextWatcher: SetlistNameTextWatcher = SetlistNameTextWatcher()

    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var setlistNameInputLayout: TextInputLayout
    private lateinit var setlistNameInput: TextView
    private var setlistSongs: List<SongItem> = listOf()

    private var intentSetlistName: String? = null
    private var setlistNames: Collection<String> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setlist_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intentSetlistName = requireActivity().intent.getStringExtra("setlistName")
        setlistNames = SetlistsContext.getSetlistItems().map { setlistItem -> setlistItem.name }

        setlistNameInputLayout = view.findViewById(R.id.tv_setlist_name)
        setlistNameInput = view.findViewById(R.id.tin_setlist_name)

        if (args.selectedSongs != null) {
            setlistSongs = SongsContext.getSongItems()
                .filter { songItem -> args.selectedSongs!!.contains(songItem.title) }

            setlistNameInput.text = args.setlistName
        } else if (intentSetlistName != null) {
            setlistNameInput.text = intentSetlistName

            val setlist = SetlistsContext.getSetlist(intentSetlistName!!)

            setlistSongs = SongsContext.getSongItems()
                .filter { songItem -> setlist.songIds.contains(songItem.id) }
        }

        songsRecyclerView = view.findViewById<RecyclerView>(R.id.rcv_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupListeners(view)
    }

    override fun onResume() {
        super.onResume()

        songsRecyclerView.adapter = SongItemsAdapter(setlistSongs.toMutableList())
    }

    private fun setupListeners(view: View) {
        setlistNameInput.addTextChangedListener(setlistNameTextWatcher)

        view.findViewById<Button>(R.id.btn_pick_setlist_songs).setOnClickListener {
            val action = SetlistEditorFragmentDirections.actionSetlistEditorToSetlistEditorSongs(
                selectedSongs = setlistSongs.map { songItem -> songItem.title }.toTypedArray(),
                setlistName = setlistNameInput.text.toString()
            )

            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.btn_save_setlist).setOnClickListener {
            if (saveSetlist()) {
                requireActivity().finish()
            }
        }
    }

    private fun validateSetlistName(songTitle: String): NameValidationState {
        return if (songTitle.isBlank()) {
            NameValidationState.EMPTY
        } else if (intentSetlistName != songTitle && setlistNames.contains(songTitle)) {
            NameValidationState.ALREADY_IN_USE
        } else {
            NameValidationState.VALID
        }
    }

    private fun saveSetlist(): Boolean {
        val setlistName = setlistNameInput.text.toString()

        if (validateSetlistName(setlistName) != NameValidationState.VALID) {
            setlistNameInput.text = setlistName
            setlistNameInput.requestFocus()
            return false
        }

        if (setlistSongs.isEmpty()) {
            val toast = Toast.makeText(
                requireContext(),
                "Empty setlists are not allowed!",
                Toast.LENGTH_SHORT
            )
            toast.show()
            return false
        }

        val songIds = setlistSongs.map { songItem -> songItem.id }
        SetlistsContext.saveSetlist(setlistName, songIds)

        return true
    }

    inner class SetlistNameTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val newText = s.toString()

            when (validateSetlistName(newText)) {
                NameValidationState.EMPTY -> {
                    setlistNameInputLayout.error = " "
                    setlistNameInput.error = "Please enter setlist name"
                }
                NameValidationState.ALREADY_IN_USE -> {
                    setlistNameInputLayout.error = " "
                    setlistNameInput.error = "Setlist name already in use"
                }
                NameValidationState.VALID -> {
                    setlistNameInputLayout.error = null
                    setlistNameInput.error = null
                }
            }
        }
    }

}
