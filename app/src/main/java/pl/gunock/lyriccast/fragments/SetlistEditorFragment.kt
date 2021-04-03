/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 8:48 PM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.SetlistSongItemsAdapter
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.LyricCastViewModel
import pl.gunock.lyriccast.datamodel.LyricCastViewModelFactory
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.SetlistSongCrossRef
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.helpers.KeyboardHelper
import pl.gunock.lyriccast.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.SongItem


class SetlistEditorFragment : Fragment() {

    private companion object {
        const val TAG = "SetlistEditorFragment"
    }

    private val args: SetlistEditorFragmentArgs by navArgs()
    private lateinit var repository: LyricCastRepository
    private val lyricCastViewModel: LyricCastViewModel by viewModels {
        LyricCastViewModelFactory((requireActivity().application as LyricCastApplication).repository)
    }

    private val setlistNameTextWatcher: SetlistNameTextWatcher = SetlistNameTextWatcher()

    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var setlistNameInputLayout: TextInputLayout
    private lateinit var setlistNameInput: TextView

    private var setlistSongs: List<SongItem> = listOf()
    private lateinit var songItemsAdapter: SetlistSongItemsAdapter
    private lateinit var selectionTracker: SelectionTracker<SetlistSongItemsAdapter.ViewHolder>

    private var intentSetlist: Setlist? = null
    private lateinit var setlistNames: Set<String>

    private lateinit var menu: Menu

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                override fun isLongPressDragEnabled(): Boolean {
                    return false
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    holder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val from = holder.adapterPosition
                    val to = target.adapterPosition
                    songItemsAdapter.moveItem(from, to)
                    songItemsAdapter.notifyItemMoved(from, to)

                    return true
                }

                override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {}
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = (requireActivity().application as LyricCastApplication).repository
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_setlist_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intentSetlist = requireActivity().intent.getParcelableExtra("setlist")

        setlistNameInputLayout = view.findViewById(R.id.tv_setlist_name)
        setlistNameInput = view.findViewById(R.id.tin_setlist_name)

        setlistNameInput.filters = arrayOf(InputFilter.LengthFilter(30))

        lyricCastViewModel.allSetlists.observe(requireActivity()) { setlists ->
            setlistNames = setlists.map { setlist -> setlist.name }.toSet()
        }

        if (args.setlistWithSongs != null) {
            setlistSongs =
                runBlocking { repository.getSongsAndCategories(args.setlistWithSongs!!.songs) }
                    .map { song -> SongItem(song) }

            setlistNameInput.text = args.setlistWithSongs!!.setlist.name
        } else if (intentSetlist != null) {
            val setlistWithSongs =
                runBlocking { repository.getSetlistWithSongs(intentSetlist!!.id)!! }

            setlistNameInput.text = intentSetlist!!.name

            setlistSongs = runBlocking { repository.getSongsAndCategories(setlistWithSongs.songs) }
                .map { song -> SongItem(song) }
        }

        songsRecyclerView = view.findViewById<RecyclerView>(R.id.rcv_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupListeners(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> removeSelectedSongs()
            R.id.menu_duplicate -> duplicateSong()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        setupSongList()
    }

    private fun setupListeners(view: View) {
        setlistNameInput.addTextChangedListener(setlistNameTextWatcher)

        setlistNameInput.setOnFocusChangeListener { view_, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view_)
            }
        }

        view.findViewById<Button>(R.id.btn_pick_setlist_songs).setOnClickListener {
            val action = SetlistEditorFragmentDirections.actionSetlistEditorToSetlistEditorSongs(
                setlistWithSongs = createSetlistWithSongs()
            )

            findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.btn_save_setlist).setOnClickListener {
            if (saveSetlist()) {
                requireActivity().finish()
            }
        }
    }

    private fun setupSongList() {
        val onHandleTouchListener =
            TouchAdapterItemListener { holder: SetlistSongItemsAdapter.ViewHolder, view, event ->
                view.requestFocus()
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }

                return@TouchAdapterItemListener true
            }

        selectionTracker = SelectionTracker(songsRecyclerView, this::onSetlistClick)
        songItemsAdapter = SetlistSongItemsAdapter(
            requireContext(),
            setlistSongs.toMutableList(),
            selectionTracker = selectionTracker,
            onHandleTouchListener = onHandleTouchListener
        )
        songsRecyclerView.adapter = songItemsAdapter
        itemTouchHelper.attachToRecyclerView(songsRecyclerView)
    }

    private fun onSetlistClick(
        holder: SetlistSongItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        if (isLongClick || selectionTracker.count != 0) {
            val item: SongItem = songItemsAdapter.songItems[position]
            selectSong(item, holder)
            return true
        }
        return false
    }

    private fun createSetlistWithSongs(): SetlistWithSongs {
        val setlistName = setlistNameInput.text.toString()
        val setlist = if (args.setlistWithSongs != null) {
            Setlist(args.setlistWithSongs!!.setlist.setlistId, setlistName)
        } else {
            Setlist(null, setlistName)
        }

        val songs = songItemsAdapter.songItems
            .map { item -> item.song }
            .distinct()

        val crossRef: List<SetlistSongCrossRef> = songItemsAdapter.songItems
            .mapIndexed { index, item ->
                SetlistSongCrossRef(setlist.id, item.song.id, index)
            }

        return SetlistWithSongs(setlist, songs, crossRef)
    }

    private fun validateSetlistName(name: String): NameValidationState {
        if (name.isBlank()) {
            return NameValidationState.EMPTY
        }

        val isAlreadyInUse = intentSetlist?.name != name && setlistNames.contains(name)
        return if (isAlreadyInUse) {
            NameValidationState.ALREADY_IN_USE
        } else {
            NameValidationState.VALID
        }
    }

    private fun saveSetlist(): Boolean {
        val setlistName = setlistNameInput.text.toString().trim()

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

        val setlistWithSongs = createSetlistWithSongs()
        lyricCastViewModel.upsertSetlist(setlistWithSongs)
        Log.i(TAG, "Created setlist: $setlistWithSongs")
        return true
    }

    private fun selectSong(item: SongItem, holder: SetlistSongItemsAdapter.ViewHolder) {
        item.isSelected = !item.isSelected
        holder.checkBox.isChecked = item.isSelected

        when (selectionTracker.countAfter) {
            0 -> {
                if (songItemsAdapter.showCheckBox.value!!) {
                    songItemsAdapter.showCheckBox.value = false
                }
                showMenuActions(showDelete = false, showDuplicate = false)
            }
            1 -> {
                if (!songItemsAdapter.showCheckBox.value!!) {
                    songItemsAdapter.showCheckBox.value = true
                }
                showMenuActions()
            }
            2 -> showMenuActions(showDuplicate = false)
        }

    }

    private fun showMenuActions(showDelete: Boolean = true, showDuplicate: Boolean = true) {
        menu.findItem(R.id.menu_delete).isVisible = showDelete
        menu.findItem(R.id.menu_duplicate).isVisible = showDuplicate
    }

    private fun removeSelectedSongs(): Boolean {
        songItemsAdapter.removeSelectedItems()
        resetSelection()

        return true
    }

    private fun duplicateSong(): Boolean {
        songItemsAdapter.duplicateSelectedItem()
        resetSelection()

        return true
    }

    private fun resetSelection() {
        if (songItemsAdapter.showCheckBox.value!!) {
            songItemsAdapter.showCheckBox.value = false
        }
        selectionTracker.reset()

        showMenuActions(showDelete = false, showDuplicate = false)
    }

    inner class SetlistNameTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val newText = s.toString().trim()

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
