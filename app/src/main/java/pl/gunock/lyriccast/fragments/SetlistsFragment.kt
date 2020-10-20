/*
 * Created by Tomasz Kiljańczyk on 10/20/20 10:55 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/20/20 10:09 PM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.activities.SetlistControlsActivity
import pl.gunock.lyriccast.adapters.SetlistListAdapter
import pl.gunock.lyriccast.listeners.InputTextChangeListener
import pl.gunock.lyriccast.listeners.RecyclerItemClickListener
import pl.gunock.lyriccast.listeners.SpinnerItemSelectedListener
import pl.gunock.lyriccast.models.SetlistModel


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

        SetlistsContext.setupSetlistListAdapter()

        SetlistsContext.setlistListAdapter = SetlistListAdapter(SetlistsContext.setlistItemList)
        view.findViewById<RecyclerView>(R.id.recycler_view_setlists).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SetlistsContext.setlistListAdapter
        }

        setupListeners(view)

        if (SetlistsContext.setlistList.isEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val setlistList: List<SetlistModel> = SetlistsContext.loadSetlists()
                lifecycleScope.launch(Dispatchers.Main) {
                    SetlistsContext.fillSetlistList(setlistList)
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
        // TODO: Adapt for setlists
        view.findViewById<RecyclerView>(R.id.recycler_view_setlists).addOnItemTouchListener(
            RecyclerItemClickListener(context) { _, position ->
                SetlistsContext.pickSetlist(position)
                val intent = Intent(context, SetlistControlsActivity::class.java)
                startActivity(intent)
            })

        searchView!!.editText!!.addTextChangedListener(InputTextChangeListener {
            SetlistsContext.filterSetlists(it, categorySpinner!!.selectedItem.toString())
        })

        categorySpinner!!.onItemSelectedListener = SpinnerItemSelectedListener { _, _ ->
            SetlistsContext.filterSetlists(
                searchView!!.editText!!.editableText.toString(),
                categorySpinner!!.selectedItem.toString()
            )
        }
    }
}
