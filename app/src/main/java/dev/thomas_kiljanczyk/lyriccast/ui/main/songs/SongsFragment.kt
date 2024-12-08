/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main.songs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.FragmentSongsBinding
import dev.thomas_kiljanczyk.lyriccast.domain.models.SongItem
import dev.thomas_kiljanczyk.lyriccast.shared.extensions.hideKeyboard
import dev.thomas_kiljanczyk.lyriccast.shared.extensions.registerForActivityResult
import dev.thomas_kiljanczyk.lyriccast.shared.utils.DialogFragmentUtils
import dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.SetlistEditorActivity
import dev.thomas_kiljanczyk.lyriccast.ui.shared.adapters.CategorySpinnerAdapter
import dev.thomas_kiljanczyk.lyriccast.ui.shared.adapters.SongItemsAdapter
import dev.thomas_kiljanczyk.lyriccast.ui.shared.listeners.InputTextChangedListener
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.MappedItemKeyProvider
import dev.thomas_kiljanczyk.lyriccast.ui.shared.selection.SimpleItemDetailsLookup
import dev.thomas_kiljanczyk.lyriccast.ui.song_controls.SongControlsActivity
import dev.thomas_kiljanczyk.lyriccast.ui.song_editor.SongEditorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class SongsFragment : Fragment() {
    private companion object {
        const val TAG = "SongsFragment"
    }

    private val viewModel: SongsModel by activityViewModels()

    private lateinit var songItemsAdapter: SongItemsAdapter
    private lateinit var binding: FragmentSongsBinding

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = SongsActionModeCallback()

    private val exportChooserResultLauncher = registerForActivityResult(this::exportSelectedSongs)

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private lateinit var tracker: SelectionTracker<Long>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swtSelectedSongs.visibility = View.GONE

        setupCategorySpinner()
        setupRecyclerView()
        setupListeners()
    }

    override fun onDestroy() {
        actionMode?.finish()

        super.onDestroy()
    }

    override fun onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment: Boolean) {
        if (!isPrimaryNavigationFragment) {
            actionMode?.finish()
        }

        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment)
    }

    private fun setupListeners() {
        binding.edSongTitleFilter.addTextChangedListener(InputTextChangedListener {
            viewModel.searchValues.songTitle = it
        })

        binding.edSongTitleFilter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                view.hideKeyboard()
            }
        }
    }

    @SuppressLint("CutPasteId")
    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(binding.dropdownCategory.context)

        binding.dropdownCategory.setAdapter(categorySpinnerAdapter)
        binding.dropdownCategory.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val categoryItem = categorySpinnerAdapter.getItem(position)
                val categoryId: String = categoryItem.category.id
                viewModel.searchValues.categoryId = categoryId
            }

        viewModel.categories
            .onEach {
                categorySpinnerAdapter.submitCollection(it)

                val firstCategoryName = categorySpinnerAdapter.getItem(0).category.name
                withContext(Dispatchers.Main) {
                    binding.dropdownCategory.setText(firstCategoryName)
                }
            }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)
    }

    private fun setupRecyclerView() {
        songItemsAdapter = SongItemsAdapter(binding.rcvSongs.context)
        songItemsAdapter.onItemClickListener = this::onPickSong

        binding.rcvSongs.setHasFixedSize(true)
        binding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSongs.adapter = songItemsAdapter

        tracker = SelectionTracker.Builder(
            "selection",
            binding.rcvSongs,
            MappedItemKeyProvider(binding.rcvSongs),
            SimpleItemDetailsLookup(binding.rcvSongs),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(SongSelectionObserver())

        viewModel.songs
            .onEach { songItemsAdapter.submitList(it) }
            .launchIn(lifecycleScope)
    }

    private fun onPickSong(item: SongItem?) {
        if (!tracker.selection.isEmpty) {
            return
        }

        item ?: return

        val intent = Intent(requireContext(), SongControlsActivity::class.java)
        intent.putExtra("songId", item.song.id)
        startActivity(intent)
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

            2 -> showMenuActions(showEdit = false)
        }
    }

    private fun editSelectedSong() {
        val selectedItem = viewModel.getSelectedSong()

        Log.v(TAG, "Editing song : ${selectedItem.song}")
        val intent = Intent(requireContext(), SongEditorActivity::class.java)
        intent.putExtra("songId", selectedItem.song.id)
        startActivity(intent)
    }

    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        exportChooserResultLauncher.launch(chooserIntent)
    }

    private fun exportSelectedSongs(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }

        lifecycleScope.launch(Dispatchers.Default) {
            val uri: Uri = result.data!!.data!!

            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    childFragmentManager,
                    R.string.main_activity_export_preparing_data
                )

            val outputStream = requireActivity().contentResolver.openOutputStream(uri)!!
            val exportMessageFlow = viewModel.exportSongs(
                requireActivity().cacheDir.canonicalPath,
                outputStream
            )

            exportMessageFlow.onEach { dialogFragment.setMessage(it) }
                .onCompletion {
                    withContext(Dispatchers.IO) {
                        outputStream.close()
                    }
                    dialogFragment.dismiss()
                }.flowOn(Dispatchers.Main)
                .launchIn(dialogFragment.lifecycleScope)
        }
    }

    private fun createAdhocSetlist() {
        val setlistSongs = viewModel.getSelectedSongIds().toTypedArray()
        val intent = Intent(context, SetlistEditorActivity::class.java)
        intent.putExtra("setlistSongs", setlistSongs)
        startActivity(intent)
    }

    private fun showMenuActions(
        showGroupActions: Boolean = true,
        showEdit: Boolean = true
    ) {
        actionMenu?.apply {
            findItem(R.id.action_menu_delete).isVisible = showGroupActions
            findItem(R.id.action_menu_export_selected).isVisible = showGroupActions
            findItem(R.id.action_menu_add_setlist).isVisible = showGroupActions
            findItem(R.id.action_menu_edit).isVisible = showEdit
        }
    }

    private fun resetSelection() {
        tracker.clearSelection()
        viewModel.hideSelectionCheckboxes()
        notifyAllItemsChanged()
    }

    private fun notifyAllItemsChanged() {
        songItemsAdapter.notifyItemRangeChanged(0, songItemsAdapter.itemCount, true)
    }


    private inner class SongsActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_main, menu)
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
            showMenuActions(showGroupActions = false, showEdit = false)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            lifecycleScope.launch(Dispatchers.Default) {
                val result = when (item.itemId) {
                    R.id.action_menu_delete -> {
                        viewModel.deleteSelectedSongs()
                        true
                    }

                    R.id.action_menu_export_selected -> {
                        startExport()
                        true
                    }

                    R.id.action_menu_edit -> {
                        editSelectedSong()
                        true
                    }

                    R.id.action_menu_add_setlist -> {
                        createAdhocSetlist()
                        true
                    }

                    else -> false
                }

                if (result) {
                    withContext(Dispatchers.Main) { mode.finish() }
                }
            }

            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            actionMenu = null
            resetSelection()

            onBackPressedCallback?.remove()
            onBackPressedCallback = null
        }
    }


    private inner class SongSelectionObserver : SelectionTracker.SelectionObserver<Long>() {
        override fun onItemStateChanged(key: Long, selected: Boolean) {
            super.onItemStateChanged(key, selected)
            if (viewModel.selectSong(key, selected)) {
                onSelectSong()
            }
        }
    }
}
