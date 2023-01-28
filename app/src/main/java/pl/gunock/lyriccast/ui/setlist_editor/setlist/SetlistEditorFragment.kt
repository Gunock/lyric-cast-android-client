/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 21:22
 */

package pl.gunock.lyriccast.ui.setlist_editor.setlist

import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.FragmentSetlistEditorBinding
import pl.gunock.lyriccast.shared.enums.NameValidationState
import pl.gunock.lyriccast.shared.extensions.hideKeyboard
import pl.gunock.lyriccast.ui.shared.listeners.TouchAdapterItemListener
import pl.gunock.lyriccast.ui.shared.selection.MappedItemKeyProvider
import pl.gunock.lyriccast.ui.shared.selection.SelectionViewHolder
import pl.gunock.lyriccast.ui.shared.selection.SimpleItemDetailsLookup

@AndroidEntryPoint
class SetlistEditorFragment : Fragment() {

    private val args: SetlistEditorFragmentArgs by navArgs()
    private val viewModel: SetlistEditorModel by activityViewModels()
    private lateinit var binding: FragmentSetlistEditorBinding

    private val setlistNameTextWatcher: SetlistNameTextWatcher by lazy {
        SetlistNameTextWatcher(resources, binding, viewModel)
    }

    private lateinit var setlistSongItemsAdapter: SetlistSongItemsAdapter


    private var toast: Toast? = null

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = SetlistEditorActionModeCallback()

    private val itemTouchHelper by lazy { ItemTouchHelper(SetlistItemTouchCallback()) }

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private lateinit var tracker: SelectionTracker<Long>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetlistEditorBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroy() {
        itemTouchHelper.attachToRecyclerView(null)
        actionMode?.finish()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSetlist()
        setupListeners()
        setupRecyclerView()

        viewModel.selectedSongPosition
            .onEach { setlistSongItemsAdapter.notifyItemChanged(it, true) }
            .launchIn(lifecycleScope)
    }

    private fun loadSetlist() {
        val intentSetlistId =
            requireActivity().intent.getStringExtra("setlistId")

        val intentSetlistPresentation = requireActivity().intent
            .getStringArrayExtra("setlistSongs")

        binding.edSetlistName.filters = arrayOf(
            InputFilter.LengthFilter(resources.getInteger(R.integer.ed_max_length_setlist_name))
        )

        when {
            args.setlistId != null -> {
                viewModel.loadSetlist(
                    args.setlistId!!,
                    args.setlistName!!,
                    args.presentation!!.toList()
                )
                binding.edSetlistName.setText(viewModel.setlistName)
            }
            intentSetlistId != null -> {
                viewModel.loadEditedSetlist(intentSetlistId)
                binding.edSetlistName.setText(viewModel.setlistName)
            }
            intentSetlistPresentation != null -> {
                viewModel.loadAdhocSetlist(intentSetlistPresentation.toList())
            }
        }
    }

    private fun setupListeners() {
        binding.edSetlistName.addTextChangedListener(setlistNameTextWatcher)

        binding.edSetlistName.setOnFocusChangeListener { view_, hasFocus ->
            if (!hasFocus) view_.hideKeyboard()
        }

        binding.btnPickSetlistSongs.setOnClickListener {
            actionMode?.finish()

            val presentation: Array<String> = viewModel.songs.value
                .map { it.song.id }
                .toTypedArray()

            val action = SetlistEditorFragmentDirections.actionSetlistEditorToSetlistEditorSongs(
                setlistId = viewModel.setlistId,
                setlistName = viewModel.setlistName,
                presentation = presentation
            )

            findNavController().navigate(action)
        }

        binding.btnSaveSetlist.setOnClickListener {
            saveSetlist()
        }
    }

    private fun setupRecyclerView() {
        val onHandleTouchListener =
            TouchAdapterItemListener { holder: SelectionViewHolder<SetlistSongItem>, view, event ->
                view.requestFocus()
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder)
                }

                return@TouchAdapterItemListener true
            }

        setlistSongItemsAdapter = SetlistSongItemsAdapter(
            binding.rcvSongs.context,
            onHandleTouchListener = onHandleTouchListener
        )

        binding.rcvSongs.setHasFixedSize(true)
        binding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSongs.adapter = setlistSongItemsAdapter

        tracker = SelectionTracker.Builder(
            "selection",
            binding.rcvSongs,
            MappedItemKeyProvider(binding.rcvSongs),
            SimpleItemDetailsLookup(binding.rcvSongs),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onItemStateChanged(key: Long, selected: Boolean) {
                    super.onItemStateChanged(key, selected)
                    if (viewModel.selectSong(key, selected)) {
                        onSelectSong()
                    }
                }
            }
        )

        itemTouchHelper.attachToRecyclerView(binding.rcvSongs)

        viewModel.songs
            .onEach { setlistSongItemsAdapter.submitList(it) }
            .launchIn(lifecycleScope)
    }

    private fun checkSetlistNameValidity(): Boolean {
        if (viewModel.validateSetlistName(viewModel.setlistName) != NameValidationState.VALID) {
            binding.edSetlistName.setText(viewModel.setlistName)
            binding.tinSetlistName.requestFocus()
            return false
        }
        return true
    }

    private fun saveSetlist() {
        if (!checkSetlistNameValidity()) {
            return
        }

        if (viewModel.songs.value.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.Default) { viewModel.saveSetlist() }
            requireActivity().finish()
        } else {
            toast?.cancel()
            toast = Toast.makeText(
                requireContext(),
                getString(R.string.setlist_editor_empty_warning),
                Toast.LENGTH_SHORT
            ).apply { show() }
            requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    private fun onSelectSong() {
        when (tracker.selection.size()) {
            0 -> {
                actionMode?.finish()
            }
            1 -> {
                if (actionMode == null) {
                    actionMode = (requireActivity() as AppCompatActivity)
                        .startSupportActionMode(actionModeCallback)
                    viewModel.showSelectionCheckboxes()
                    notifyAllItemsChanged()
                }

                showMenuActions()
            }
            2 -> showMenuActions(showDuplicate = false)
        }
    }

    private fun showMenuActions(showDelete: Boolean = true, showDuplicate: Boolean = true) {
        actionMenu ?: return
        actionMenu!!.findItem(R.id.action_menu_delete).isVisible = showDelete
        actionMenu!!.findItem(R.id.menu_duplicate).isVisible = showDuplicate
    }

    private fun removeSelectedSongs(): Boolean {
        viewModel.removeSelectedSongs()

        return true
    }

    private fun duplicateSong(): Boolean {
        viewModel.duplicateSelectedSong()

        return true
    }

    private fun resetSelection() {
        tracker.clearSelection()
        viewModel.hideSelectionCheckboxes()
        notifyAllItemsChanged()
    }

    private fun notifyAllItemsChanged() {
        setlistSongItemsAdapter.notifyItemRangeChanged(0, setlistSongItemsAdapter.itemCount, true)
    }

    private inner class SetlistEditorActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_setlist_editor, menu)
            mode.title = ""
            actionMenu = menu

            onBackPressedCallback =
                requireActivity().onBackPressedDispatcher.addCallback(requireActivity()) {
                    resetSelection()
                    onBackPressedCallback?.remove()
                    onBackPressedCallback = null
                }

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            showMenuActions()
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val result = when (item.itemId) {
                R.id.action_menu_delete -> removeSelectedSongs()
                R.id.menu_duplicate -> duplicateSong()
                else -> false
            }

            if (result) {
                mode.finish()
            }

            return result
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            actionMenu = null
            resetSelection()

            onBackPressedCallback?.remove()
            onBackPressedCallback = null
        }

    }

    private inner class SetlistItemTouchCallback :
        ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

        override fun onMove(
            recyclerView: RecyclerView,
            holder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = holder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition

            viewModel.moveSong(from, to)
            setlistSongItemsAdapter.notifyItemMoved(from, to)

            return true
        }

        override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {}
    }
}
