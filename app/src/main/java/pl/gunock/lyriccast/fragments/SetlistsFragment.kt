/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 12:22 AM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.SetlistControlsActivity
import pl.gunock.lyriccast.activities.SetlistEditorActivity
import pl.gunock.lyriccast.adapters.SetlistItemsAdapter
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.helpers.KeyboardHelper
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.SetlistItem
import kotlin.system.measureTimeMillis


class SetlistsFragment : Fragment() {
    private companion object {
        const val TAG = "SetlistsFragment"
    }

    private lateinit var repository: LyricCastRepository

    private lateinit var menu: Menu
    private lateinit var searchViewEditText: EditText
    private lateinit var setlistRecyclerView: RecyclerView

    private var setlistItems: Set<SetlistItem> = setOf()

    private lateinit var setlistItemsAdapter: SetlistItemsAdapter
    private lateinit var selectionTracker: SelectionTracker<SetlistItemsAdapter.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        repository = (requireActivity().application as LyricCastApplication).repository
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

        val deleteActionItem = menu.findItem(R.id.menu_delete)
        deleteActionItem.isVisible = false

        val editActionItem = menu.findItem(R.id.menu_edit)
        editActionItem.isVisible = false
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
        setlistItems = runBlocking { repository.getSetlists() }
            .map { setlist -> SetlistItem(setlist) }
            .toSet()

        val setlistRecyclerView = requireView().findViewById<RecyclerView>(R.id.rcv_setlists)
        selectionTracker = SelectionTracker(setlistRecyclerView, this::onSetlistClick)
        setlistItemsAdapter = SetlistItemsAdapter(
            requireContext(),
            setlistItems = setlistItems.toMutableList(),
            selectionTracker = selectionTracker
        )

        setlistRecyclerView.adapter = setlistItemsAdapter
    }

    private fun onSetlistClick(
        holder: SetlistItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = setlistItemsAdapter.setlistItems[position]
        if (!isLongClick && selectionTracker.count == 0) {
            pickSetlist(item)
        } else {
            selectSetlist(item, holder)
        }
        return true
    }

    private fun setupListeners() {
        searchViewEditText.addTextChangedListener(InputTextChangedListener { newText ->
            filterSetlists(
                newText
            )
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
        item: SetlistItem,
        holder: SetlistItemsAdapter.ViewHolder
    ) {
        when (selectionTracker.countAfter) {
            0 -> {
                if (setlistItemsAdapter.showCheckBox.value!!) {
                    setlistItemsAdapter.showCheckBox.value = false
                }

                val deleteActionItem = menu.findItem(R.id.menu_delete)
                deleteActionItem.isVisible = false

                val editActionItem = menu.findItem(R.id.menu_edit)
                editActionItem.isVisible = false
            }
            1 -> {
                if (!setlistItemsAdapter.showCheckBox.value!!) {
                    setlistItemsAdapter.showCheckBox.value = true
                }

                val deleteActionItem = menu.findItem(R.id.menu_delete)
                deleteActionItem.isVisible = true

                val editActionItem = menu.findItem(R.id.menu_edit)
                editActionItem.isVisible = true
            }
            2 -> {
                val deleteActionItem = menu.findItem(R.id.menu_delete)
                deleteActionItem.isVisible = true

                val editActionItem = menu.findItem(R.id.menu_edit)
                editActionItem.isVisible = false
            }
        }

        item.isSelected = !item.isSelected
        holder.checkBox.isChecked = item.isSelected

    }

    private fun editSelectedSetlist(): Boolean {
        val selectedItem = setlistItemsAdapter.setlistItems
            .first { setlistItem -> setlistItem.isSelected }

        val intent = Intent(requireContext(), SetlistEditorActivity::class.java)
        intent.putExtra("setlist", selectedItem.setlist)
        startActivity(intent)

        setlistItemsAdapter.showCheckBox.value = false
        selectionTracker.reset()

        return true
    }

    private fun deleteSelectedSetlists(): Boolean {
        val selectedSetlists = setlistItemsAdapter.setlistItems
            .filter { item -> item.isSelected }
            .map { item -> item.setlist.id }

        runBlocking { repository.deleteSetlists(selectedSetlists) }

        val remainingSetlists = setlistItemsAdapter.setlistItems
            .filter { item -> !selectedSetlists.contains(item.setlist.id) }
        setlistItemsAdapter.showCheckBox.value = false

        setlistItemsAdapter.setlistItems.clear()
        setlistItemsAdapter.setlistItems.addAll(remainingSetlists)
        setlistItemsAdapter.notifyDataSetChanged()

        selectionTracker.reset()

        return true
    }

    private fun filterSetlists(name: String) {
        Log.v(TAG, "filterSetlists invoked")

        val normalizedName = name.normalize()

        val duration = measureTimeMillis {
            setlistItemsAdapter.setlistItems = setlistItems.filter { item ->
                item.normalizedName.contains(normalizedName, ignoreCase = true)
            }.toMutableList()
        }
        Log.v(TAG, "Filtering took : ${duration}ms")

        setlistItemsAdapter.notifyDataSetChanged()
    }
}
