/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/14/20 11:44 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.SetlistListAdapter
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.RecyclerItemClickListener
import pl.gunock.lyriccast.listeners.SpinnerItemSelectedListener
import pl.gunock.lyriccast.models.SetlistModel


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SetlistsFragment : Fragment() {

    private var castContext: CastContext? = null

    private var searchView: TextInputLayout? = null
    private var categorySpinner: Spinner? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        castContext = CastContext.getSharedInstance()
//
//        searchView = view.findViewById(R.id.text_view_filter_songs)
//        categorySpinner = view.findViewById(R.id.spinner_category)
//
        val exampleSetlist1 = SetlistModel()
        exampleSetlist1.name = "Example1"
        val exampleSetlist2 = SetlistModel()
        exampleSetlist2.name = "Example2"
        SongsContext.setlistList.add(exampleSetlist1)
        SongsContext.setlistList.add(exampleSetlist2)

        SongsContext.setlistListAdapter = SetlistListAdapter(SongsContext.setlistList)
//
        view.findViewById<RecyclerView>(R.id.recycler_view_setlists).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SongsContext.setlistListAdapter
        }
//
//        setupListeners(view)
//
//        if (SongsContext.songsList.isEmpty()) {
//            SongsContext.loadSongsMetadata()
//
//            val categorySpinnerAdapter = ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_spinner_item,
//                SongsContext.categories.toList()
//            )
//            categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//            categorySpinner!!.apply {
//                adapter = categorySpinnerAdapter
//            }
//        }
    }

    private fun setupListeners(view: View) {
        view.findViewById<RecyclerView>(R.id.recycler_view_songs).addOnItemTouchListener(
            RecyclerItemClickListener(context) { _, position ->
                SongsContext.pickSong(position)
                findNavController().navigate(R.id.action_SongListFragment_to_ControlsFragment)
            })

        searchView!!.editText!!.addTextChangedListener(InputTextChangeListener {
            SongsContext.filterSongs(it, categorySpinner!!.selectedItem.toString())
        })

        categorySpinner!!.onItemSelectedListener =
            SpinnerItemSelectedListener { _, _ ->
                SongsContext.filterSongs(
                    searchView!!.editText!!.editableText.toString(),
                    categorySpinner!!.selectedItem.toString()
                )
            }
    }
}
