/*
 * Created by Tomasz Kiljanczyk on 4/3/21 9:09 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/3/21 9:09 PM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.SetlistControlsActivity
import pl.gunock.lyriccast.activities.SetlistEditorActivity
import pl.gunock.lyriccast.adapters.SetlistItemsAdapter
import pl.gunock.lyriccast.datamodel.LyricCastViewModel
import pl.gunock.lyriccast.datamodel.LyricCastViewModelFactory
import pl.gunock.lyriccast.helpers.KeyboardHelper
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.SetlistItem


class SetlistsFragment : Fragment() {
    private val lyricCastViewModel: LyricCastViewModel by viewModels {
        LyricCastViewModelFactory(
            requireContext(),
            (requireActivity().application as LyricCastApplication).repository
        )
    }

    private lateinit var menu: Menu
    private lateinit var searchViewEditText: EditText
    private lateinit var setlistRecyclerView: RecyclerView

    private lateinit var setlistItemsAdapter: SetlistItemsAdapter
    private lateinit var selectionTracker: SelectionTracker<SetlistItemsAdapter.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (selectionTracker.count > 0) {
                        setlistItemsAdapter.resetSelection()
                        resetSelection()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchView: TextInputLayout = view.findViewById(R.id.tv_filter_setlists)
        searchViewEditText = searchView.editText!!

        setlistRecyclerView = view.findViewById<RecyclerView>(R.id.rcv_setlists).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)

        showMenuActions(showDelete = false, showEdit = false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> deleteSelectedSetlists()
            R.id.menu_edit -> editSelectedSetlist()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        setupSetlists()

        searchViewEditText.setText("")
    }

    private fun setupSetlists() {
        val setlistRecyclerView = requireView().findViewById<RecyclerView>(R.id.rcv_setlists)
        selectionTracker = SelectionTracker(setlistRecyclerView, this::onSetlistClick)
        setlistItemsAdapter = SetlistItemsAdapter(
            requireContext(),
            selectionTracker = selectionTracker
        )

        setlistRecyclerView.adapter = setlistItemsAdapter

        lyricCastViewModel.allSetlists.observe(requireActivity()) { setlist ->
            setlistItemsAdapter.submitCollection(setlist ?: return@observe)
        }
    }

    private fun onSetlistClick(
        @Suppress("UNUSED_PARAMETER")
        holder: SetlistItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = setlistItemsAdapter.setlistItems[position]
        if (!isLongClick && selectionTracker.count == 0) {
            pickSetlist(item)
        } else {
            selectSetlist(item)
        }
        return true
    }

    private fun setupListeners() {
        searchViewEditText.addTextChangedListener(InputTextChangedListener { newText ->
            setlistItemsAdapter.filterItems(newText)
            resetSelection()
        })

        searchViewEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view)
            }
        }
    }

    private fun pickSetlist(item: SetlistItem) {
        val intent = Intent(context, SetlistControlsActivity::class.java)
        intent.putExtra("setlist", item.setlist)
        startActivity(intent)
    }

    private fun selectSetlist(
        item: SetlistItem
    ) {
        when (selectionTracker.countAfter) {
            0 -> {
                if (setlistItemsAdapter.showCheckBox.value!!) {
                    setlistItemsAdapter.showCheckBox.value = false
                }

                showMenuActions(showDelete = false, showEdit = false)
            }
            1 -> {
                if (!setlistItemsAdapter.showCheckBox.value!!) {
                    setlistItemsAdapter.showCheckBox.value = true
                }

                showMenuActions()
            }
            2 -> showMenuActions(showEdit = false)
        }

        item.isSelected.value = !item.isSelected.value!!
    }

    private fun editSelectedSetlist(): Boolean {
        val selectedItem = setlistItemsAdapter.setlistItems
            .first { setlistItem -> setlistItem.isSelected.value!! }

        val intent = Intent(requireContext(), SetlistEditorActivity::class.java)
        intent.putExtra("setlist", selectedItem.setlist)
        startActivity(intent)

        setlistItemsAdapter.showCheckBox.value = false
        selectionTracker.reset()

        return true
    }

    private fun deleteSelectedSetlists(): Boolean {
        val selectedSetlists = setlistItemsAdapter.setlistItems
            .filter { item -> item.isSelected.value!! }
            .map { item -> item.setlist.id }

        lyricCastViewModel.deleteSetlists(selectedSetlists)
        selectionTracker.reset()

        return true
    }

    private fun showMenuActions(showDelete: Boolean = true, showEdit: Boolean = true) {
        if (!this::menu.isInitialized) {
            return
        }

        menu.findItem(R.id.menu_delete).isVisible = showDelete
        menu.findItem(R.id.menu_edit).isVisible = showEdit
    }

    private fun resetSelection() {
        if (setlistItemsAdapter.showCheckBox.value!!) {
            setlistItemsAdapter.showCheckBox.value = false
        }

        showMenuActions(showDelete = false, showEdit = false)
    }

}
