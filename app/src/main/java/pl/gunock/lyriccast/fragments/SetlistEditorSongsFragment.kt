/*
 * Created by Tomasz Kiljańczyk on 3/15/21 3:53 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 3:05 AM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.CategoriesContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.SongItemsAdapter
import pl.gunock.lyriccast.adapters.spinner.CategorySpinnerAdapter
import pl.gunock.lyriccast.extensions.normalize
import pl.gunock.lyriccast.helpers.KeyboardHelper
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.Category
import pl.gunock.lyriccast.models.SongItem
import kotlin.system.measureTimeMillis


class SetlistEditorSongsFragment : Fragment() {
    private companion object {
        const val TAG = "SetlistEditorSongsFg"
    }

    private val args: SetlistEditorSongsFragmentArgs by navArgs()

    private lateinit var songTitleInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var selectedSongsSwitch: SwitchCompat

    private lateinit var songItemsAdapter: SongItemsAdapter
    private lateinit var selectedSongs: MutableSet<Long>
    private var songItems: Set<SongItem> = setOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedSongs = args.selectedSongs.toMutableSet()

        val searchView: TextInputLayout = view.findViewById(R.id.tv_filter_songs)
        songTitleInput = searchView.editText!!
        categorySpinner = view.findViewById(R.id.spn_category)
        selectedSongsSwitch = view.findViewById(R.id.swt_selected_songs)

        setupSongs(view)
        setupCategorySpinner()
        setupListeners()
        KeyboardHelper.hideKeyboard(view)
    }

    override fun onPause() {
        super.onPause()
        KeyboardHelper.hideKeyboard(requireView())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                updateSelectedSongs()

                val selectedSongsPrev = args.selectedSongs.toMutableList()
                val removedSongs =
                    selectedSongsPrev.filter { songId -> !selectedSongs.contains(songId) }
                val addedSongs =
                    selectedSongs.filter { songId -> !selectedSongsPrev.contains(songId) }

                selectedSongsPrev.removeAll(removedSongs)
                selectedSongsPrev.addAll(addedSongs)

                val action = SetlistEditorSongsFragmentDirections
                    .actionSetlistEditorSongsToSetlistEditor(
                        selectedSongs = selectedSongsPrev.toLongArray(),
                        setlistName = args.setlistName
                    )

                findNavController().navigate(action)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        songTitleInput.addTextChangedListener(InputTextChangedListener { newText ->
            filterSongs(newText, categorySpinner.selectedItem as Category)
        })

        categorySpinner.onItemSelectedListener =
            ItemSelectedSpinnerListener { _, _ ->
                filterSongs(
                    songTitleInput.editableText.toString(),
                    categorySpinner.selectedItem as Category
                )
            }

        selectedSongsSwitch.setOnCheckedChangeListener { _, isChecked ->
            filterSongs(
                songTitleInput.editableText.toString(),
                categorySpinner.selectedItem as Category,
                isSelected = if (isChecked) true else null
            )
        }
    }

    private fun setupCategorySpinner() {
        val categories = CategoriesContext.getCategories()

        val categorySpinnerAdapter = CategorySpinnerAdapter(
            requireContext(),
            listOf(Category("All")) + categories
        )

        categorySpinner.adapter = categorySpinnerAdapter
    }

    private fun setupSongs(view: View) {
        songItems = SongsContext.getSongItems()
        songItems.forEach { songItem ->
            songItem.isSelected = selectedSongs.contains(songItem.id)
        }

        val songsRecyclerView: RecyclerView = view.findViewById(R.id.rcv_songs)
        val selectionTracker =
            SelectionTracker(songsRecyclerView) { holder: SongItemsAdapter.ViewHolder, position: Int, _: Boolean ->
                val item: SongItem = songItemsAdapter.songItems[position]
                selectSong(item, holder)
                return@SelectionTracker true
            }

        songItemsAdapter = SongItemsAdapter(
            requireContext(),
            songItems.toMutableList(),
            showCheckBox = MutableLiveData(true),
            selectionTracker = selectionTracker
        )

        with(songsRecyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songItemsAdapter
        }
    }

    private fun selectSong(item: SongItem, holder: SongItemsAdapter.ViewHolder) {
        item.isSelected = !item.isSelected
        holder.checkBox.isChecked = item.isSelected
    }

    private fun filterSongs(
        title: String,
        category: Category = Category("All"),
        isSelected: Boolean? = null
    ) {
        Log.d(TAG, "filterSongs invoked")

        updateSelectedSongs()

        val normalizedTitle = title.normalize()

        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (isSelected != null) {
            predicates.add { songItem -> songItem.isSelected }
        }

        if (category.name != "All") {
            predicates.add { songItem -> songItem.category?.id == category.id }
        }

        predicates.add { songItem ->
            songItem.title.normalize().contains(normalizedTitle, ignoreCase = true)
        }

        val duration = measureTimeMillis {
            songItemsAdapter.songItems = songItems.filter { songItem ->
                predicates.all { predicate -> predicate(songItem) }
            }.toMutableList()
        }
        Log.d(TAG, "Filtering took : ${duration}ms")

        songItemsAdapter.notifyDataSetChanged()
    }

    private fun updateSelectedSongs() {
        for (songItem in songItemsAdapter.songItems) {
            if (songItem.isSelected) {
                selectedSongs.add(songItem.id)
            } else {
                selectedSongs.remove(songItem.id)
            }
        }
    }
}
