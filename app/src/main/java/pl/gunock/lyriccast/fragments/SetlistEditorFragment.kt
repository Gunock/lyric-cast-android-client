/*
 * Created by Tomasz Kiljańczyk on 3/15/21 1:22 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/15/21 1:22 AM
 */

package pl.gunock.lyriccast.fragments

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.adapters.SetlistSongItemsAdapter
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.listeners.ClickAdapterItemListener
import pl.gunock.lyriccast.listeners.LongClickAdapterItemListener
import pl.gunock.lyriccast.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.models.SongItem


class SetlistEditorFragment : Fragment() {

    private val args: SetlistEditorFragmentArgs by navArgs()
    private val setlistNameTextWatcher: SetlistNameTextWatcher = SetlistNameTextWatcher()

    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var setlistNameInputLayout: TextInputLayout
    private lateinit var setlistNameInput: TextView
    private lateinit var songItemsAdapter: SetlistSongItemsAdapter
    private var setlistSongs: List<SongItem> = listOf()

    private var intentSetlistName: String? = null
    private var setlistNames: Collection<String> = listOf()

    private lateinit var menu: Menu
    private var selectionCount: Int = 0

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    songItemsAdapter.moveItem(from, to)
                    songItemsAdapter.notifyItemMoved(from, to)

                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
            }
        ItemTouchHelper(simpleItemTouchCallback)
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

        intentSetlistName = requireActivity().intent.getStringExtra("setlistName")
        setlistNames = SetlistsContext.getSetlistItems().map { setlistItem -> setlistItem.name }

        setlistNameInputLayout = view.findViewById(R.id.tv_setlist_name)
        setlistNameInput = view.findViewById(R.id.tin_setlist_name)

        setlistNameInput.filters = arrayOf(InputFilter.LengthFilter(30))

        if (args.selectedSongs != null) {
            val songMap = SongsContext.getSongMap()
            setlistSongs = args.selectedSongs!!.map { songId -> SongItem(songMap[songId]!!) }

            setlistNameInput.text = args.setlistName
        } else if (intentSetlistName != null) {
            setlistNameInput.text = intentSetlistName

            val setlist = SetlistsContext.getSetlist(intentSetlistName!!)

            val songMap = SongsContext.getSongMap()
            setlistSongs = setlist.songIds.map { songId -> SongItem(songMap[songId]!!) }
        }

        songsRecyclerView = view.findViewById<RecyclerView>(R.id.rcv_songs).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
        itemTouchHelper.attachToRecyclerView(songsRecyclerView)


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

        view.findViewById<Button>(R.id.btn_pick_setlist_songs).setOnClickListener {
            val songItems = songItemsAdapter.songItems.map { songItem -> songItem.id }.toLongArray()
            val action = SetlistEditorFragmentDirections.actionSetlistEditorToSetlistEditorSongs(
                selectedSongs = songItems,
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

    private fun setupSongList() {
        val onHandleTouchListener =
            TouchAdapterItemListener { holder: SetlistSongItemsAdapter.ViewHolder, _, event ->

                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }

                return@TouchAdapterItemListener true
            }

        val onLongClickListener =
            LongClickAdapterItemListener { holder: SetlistSongItemsAdapter.ViewHolder, position: Int, _ ->
                val item = songItemsAdapter.songItems[position]
                selectSong(item, holder)
                return@LongClickAdapterItemListener true
            }

        val onClickListener =
            ClickAdapterItemListener { holder: SetlistSongItemsAdapter.ViewHolder, position: Int, _ ->
                if (selectionCount != 0) {
                    val item: SongItem = songItemsAdapter.songItems[position]
                    requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    selectSong(item, holder)
                }
            }

        songItemsAdapter = SetlistSongItemsAdapter(
            requireContext(),
            setlistSongs.toMutableList(),
            onItemClickListener = onClickListener,
            onItemLongClickListener = onLongClickListener,
            onHandleTouchListener = onHandleTouchListener
        )
        songsRecyclerView.adapter = songItemsAdapter
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

        val songIds = songItemsAdapter.songItems.map { songItem -> songItem.id }
        if (intentSetlistName == null) {
            SetlistsContext.saveSetlist(setlistName, songIds)
        } else {
            val setlist = SetlistsContext.getSetlist(intentSetlistName!!)
            SetlistsContext.saveSetlist(setlistName, songIds, setlist.id)
        }

        return true
    }

    private fun selectSong(item: SongItem, holder: SetlistSongItemsAdapter.ViewHolder) {
        if (!item.isSelected) {
            selectionCount++
        } else {
            selectionCount--
        }

        item.isSelected = !item.isSelected
        holder.checkBox.isChecked = item.isSelected

        if (selectionCount <= 0) {
            resetSelection()
            return
        }

        when (selectionCount) {
            1 -> {
                if (!songItemsAdapter.showCheckBox.value!!) {
                    songItemsAdapter.showCheckBox.value = true
                }
                if (songItemsAdapter.showHandle.value!!) {
                    songItemsAdapter.showHandle.value = false
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
        if (!songItemsAdapter.showHandle.value!!) {
            songItemsAdapter.showHandle.value = true
        }
        selectionCount = 0

        showMenuActions(showDelete = false, showDuplicate = false)
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
