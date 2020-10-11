/*
 * Created by Tomasz Kiljańczyk on 10/11/20 11:21 PM
 * Copyright (c) 2020 . All rights reserved.
 *  Last modified 10/11/20 11:06 PM
 */

package pl.gunock.lyriccast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import pl.gunock.lyriccast.models.SongLyricsModel
import pl.gunock.lyriccast.models.SongMetadataModel
import pl.gunock.lyriccast.utils.ControlAction
import pl.gunock.lyriccast.utils.MessageHelper

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SongListFragment : Fragment() {

    private var presentationIndex: Int = 0
    private var currentSongMetadata: SongMetadataModel? = null
    private var currentSongLyrics: SongLyricsModel? = null

    private var castContext: CastContext? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        castContext = CastContext.getSharedInstance()

        SongsContext.songsListAdapter = SongListAdapter(SongsContext.songsList)

        view.findViewById<RecyclerView>(R.id.recycler_view_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SongsContext.songsListAdapter
        }

        setupListeners(view)

        if (SongsContext.songsList.isEmpty()) {
            SongsContext.loadSongsMetadata()
        }
    }

    private fun setupListeners(view: View) {
        view.findViewById<RecyclerView>(R.id.recycler_view_songs).addOnItemTouchListener(
            RecyclerItemClickListener(context) { _, position ->
                currentSongMetadata = SongsContext.songsList[position]

                val songsDirectory = SongsContext.songsDirectory

                currentSongLyrics = currentSongMetadata!!.loadLyrics(songsDirectory)
                presentationIndex = 0

                sendSlide(0)
//                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            })

        view.findViewById<Button>(R.id.button_test).setOnClickListener {
            MessageHelper.sendContentMessage(castContext!!, "Android test")
        }

        view.findViewById<Button>(R.id.button_control_blank).setOnClickListener {
            MessageHelper.sendControlMessage(castContext!!, ControlAction.BLANK)
        }

        view.findViewById<Button>(R.id.button_prev).setOnClickListener {
            if (--presentationIndex < 0) {
                presentationIndex = 0
            }
            sendSlide(presentationIndex)
        }

        view.findViewById<Button>(R.id.button_next).setOnClickListener {
            if (++presentationIndex >= currentSongMetadata!!.presentation.size) {
                presentationIndex = currentSongMetadata!!.presentation.size - 1
            }
            sendSlide(presentationIndex)
        }
    }

    private fun sendSlide(index: Int) {
        currentSongMetadata!!.presentation.iterator()
        val slideTag: String = currentSongMetadata!!.presentation[index]
        MessageHelper.sendContentMessage(
            castContext!!,
            currentSongLyrics!!.lyrics[slideTag] ?: error("Slide not found")
        )
    }

}