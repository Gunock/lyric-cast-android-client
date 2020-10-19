/*
 * Created by Tomasz Kiljańczyk on 10/19/20 4:40 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/19/20 4:40 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.SetlistListAdapter
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.RecyclerItemClickListener
import pl.gunock.lyriccast.listeners.SpinnerItemSelectedListener


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SetlistsFragment : Fragment() {

    private var searchView: TextInputLayout? = null
    private var categorySpinner: Spinner? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.text_view_filter_setlists)
        categorySpinner = view.findViewById(R.id.spinner_setlist_category)

        SetlistsContext.setlistListAdapter = SetlistListAdapter(SetlistsContext.setlistList)
        view.findViewById<RecyclerView>(R.id.recycler_view_setlists).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SetlistsContext.setlistListAdapter
        }

        setupListeners(view)

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
        // TODO: Adapt for setlists
        view.findViewById<RecyclerView>(R.id.recycler_view_setlists).addOnItemTouchListener(
            RecyclerItemClickListener(context) { _, position ->
//                SongsContext.pickSong(position)
//                findNavController().navigate(R.id.action_SongListFragment_to_ControlsFragment)
            })

        searchView!!.editText!!.addTextChangedListener(InputTextChangeListener {
            SetlistsContext.filter(it, categorySpinner!!.selectedItem.toString())
        })

        categorySpinner!!.onItemSelectedListener =
            SpinnerItemSelectedListener { _, _ ->
                SetlistsContext.filter(
                    searchView!!.editText!!.editableText.toString(),
                    categorySpinner!!.selectedItem.toString()
                )
            }
    }
}
