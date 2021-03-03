/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:55 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:26 PM
 */

package pl.gunock.lyriccast.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.activities.SetlistControlsActivity
import pl.gunock.lyriccast.activities.SetlistEditorActivity
import pl.gunock.lyriccast.adapters.SetlistItemsAdapter
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.models.SetlistItem
import java.util.*
import kotlin.system.measureTimeMillis


class SetlistsFragment : Fragment() {
    private companion object {
        const val TAG = "SetlistsFragment"
    }

    private lateinit var menu: Menu
    private lateinit var searchViewEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var setlistRecyclerView: RecyclerView

    private var setlistItems: Set<SetlistItem> = setOf()
    private var selectionCount: Int = 0

    private lateinit var setlistItemsAdapter: SetlistItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        categorySpinner = view.findViewById(R.id.spn_setlist_category)

        setlistRecyclerView = view.findViewById<RecyclerView>(R.id.rcv_setlists).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupListeners()

        if (SongsContext.categories.toList().isNotEmpty()) {
            setupCategorySpinner()
        }
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
        setupCategorySpinner()

        searchViewEditText.setText("")
        categorySpinner.setSelection(0)
    }

    private fun setupSetlists() {
        setlistItems = SetlistsContext.getSetlistItems()

        val onLongClickListener =
            LongClickAdapterItemListener { holder: SetlistItemsAdapter.SetlistViewHolder, position: Int, _ ->
                val item = setlistItemsAdapter.setlistItems[position]
                selectSetlist(item, holder)
                return@LongClickAdapterItemListener true
            }

        val onClickListener =
            ClickAdapterItemListener { holder: SetlistItemsAdapter.SetlistViewHolder, position: Int, _ ->
                val item = setlistItemsAdapter.setlistItems[position]
                if (selectionCount == 0) {
                    pickSetlist(item)
                } else {
                    selectSetlist(item, holder)
                }
            }

        setlistItemsAdapter = SetlistItemsAdapter(
            setlistItems = setlistItems.toMutableList(),
            onItemLongClickListener = onLongClickListener,
            onItemClickListener = onClickListener
        )

        requireView()
            .findViewById<RecyclerView>(R.id.rcv_setlists)!!.adapter = setlistItemsAdapter
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("All") + SongsContext.categories.toList()
        )
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categorySpinnerAdapter
    }

    private fun setupListeners() {
        searchViewEditText.addTextChangedListener(InputTextChangedListener { newText ->
            filterSetlists(
                newText,
                categorySpinner.selectedItem.toString()
            )
        })

        categorySpinner.onItemSelectedListener = ItemSelectedSpinnerListener { _, _ ->
            filterSetlists(
                searchViewEditText.editableText.toString(),
                categorySpinner.selectedItem.toString()
            )
        }
    }

    private fun pickSetlist(item: SetlistItem) {
        val intent = Intent(context, SetlistControlsActivity::class.java)
        intent.putExtra("setlistName", item.name)
        startActivity(intent)
    }

    private fun selectSetlist(
        item: SetlistItem,
        holder: SetlistItemsAdapter.SetlistViewHolder
    ) {
        if (!item.isSelected) {
            selectionCount++
        } else {
            selectionCount--
        }

        var datasetChanged = false
        when (selectionCount) {
            0 -> {
                datasetChanged = true
                setlistItemsAdapter.showCheckBox = false

                val deleteActionItem = menu.findItem(R.id.menu_delete)
                deleteActionItem.isVisible = false

                val editActionItem = menu.findItem(R.id.menu_edit)
                editActionItem.isVisible = false
            }
            1 -> {
                datasetChanged = true
                setlistItemsAdapter.showCheckBox = true

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

        if (datasetChanged) {
            setlistItemsAdapter.notifyDataSetChanged()
        } else {
            holder.checkBox.isChecked = item.isSelected
        }

    }

    private fun editSelectedSetlist(): Boolean {
        val selectedSetlist = setlistItemsAdapter.setlistItems
            .first { setlistItem -> setlistItem.isSelected }

        val intent = Intent(requireContext(), SetlistEditorActivity::class.java)
        intent.putExtra("setlistName", selectedSetlist.name)
        startActivity(intent)

        setlistItemsAdapter.showCheckBox = false
        setlistItemsAdapter.notifyDataSetChanged()
        selectionCount = 0

        return true
    }

    private fun deleteSelectedSetlists(): Boolean {
        val selectedSetlists = setlistItemsAdapter.setlistItems
            .filter { setlist -> setlist.isSelected }
            .map { setlist -> setlist.name }

        SetlistsContext.deleteSetlists(selectedSetlists)

        val remainingSetlists = setlistItemsAdapter.setlistItems
            .filter { setlistItem -> !selectedSetlists.contains(setlistItem.name) }
        setlistItemsAdapter.showCheckBox = false

        setlistItemsAdapter.setlistItems.clear()
        setlistItemsAdapter.setlistItems.addAll(remainingSetlists)
        setlistItemsAdapter.notifyDataSetChanged()

        selectionCount = 0

        return true
    }

    private fun filterSetlists(name: String, category: String = "All") {
        Log.v(TAG, "filterSetlists invoked")

        val normalizedName = name.normalize()

        val predicates: MutableList<(SetlistItem) -> Boolean> = mutableListOf()

        if (category != "All") {
            predicates.add { setlistItem -> setlistItem.category == category }
        }

        predicates.add { setlistItem ->
            setlistItem.name.normalize().contains(normalizedName, ignoreCase = true)
        }

        val duration = measureTimeMillis {
            setlistItemsAdapter.setlistItems = setlistItems.filter { setlistItem ->
                predicates.all { predicate -> predicate(setlistItem) }
            }.toMutableList()
        }
        Log.v(TAG, "Filtering took : ${duration}ms")

        setlistItemsAdapter.notifyDataSetChanged()
    }
}
