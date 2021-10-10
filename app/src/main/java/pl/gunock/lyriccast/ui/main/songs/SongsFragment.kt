/*
 * Created by Tomasz Kiljanczyk on 06/10/2021, 20:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/10/2021, 17:40
 */

package pl.gunock.lyriccast.ui.main.songs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Spinner
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.FragmentSongsBinding
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.shared.extensions.hideKeyboard
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.shared.utils.DialogFragmentUtils
import pl.gunock.lyriccast.ui.setlist_editor.SetlistEditorActivity
import pl.gunock.lyriccast.ui.shared.adapters.CategorySpinnerAdapter
import pl.gunock.lyriccast.ui.shared.adapters.SongItemsAdapter
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.ui.song_controls.SongControlsActivity
import pl.gunock.lyriccast.ui.song_editor.SongEditorActivity


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel // Initializes viewModel

        binding.swtSelectedSongs.visibility = View.GONE

        viewModel.pickedSong.observe(viewLifecycleOwner, this::onPickSong)
        viewModel.numberOfSelectedSongs.observe(viewLifecycleOwner, this::onSelectSong)
        viewModel.selectedSongPosition.observe(viewLifecycleOwner) {
            songItemsAdapter.notifyItemChanged(it)
        }

        setupCategorySpinner()
        setupRecyclerView()
        setupListeners()
    }

    override fun onStop() {
        actionMode?.finish()
        super.onStop()
    }

    private fun setupListeners() {
        binding.edSongTitleFilter.addTextChangedListener(InputTextChangedListener {
            lifecycleScope.launch(Dispatchers.Default) {
                filterSongs()
            }
        })

        binding.edSongTitleFilter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                view.hideKeyboard()
            }
        }

        binding.spnCategory.onItemSelectedListener =
            ItemSelectedSpinnerListener { _, _ ->
                lifecycleScope.launch(Dispatchers.Default) {
                    filterSongs()
                }
            }
    }

    @SuppressLint("CutPasteId")
    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(requireContext())

        binding.spnCategory.adapter = categorySpinnerAdapter

        viewModel.categories.observe(viewLifecycleOwner) { categories: List<CategoryItem> ->
            lifecycleScope.launch(Dispatchers.Default) {
                categorySpinnerAdapter.submitCollection(categories)
            }
        }
    }

    private fun setupRecyclerView() {
        songItemsAdapter = SongItemsAdapter(
            requireContext(),
            selectionTracker = viewModel.selectionTracker
        )

        binding.rcvSongs.setHasFixedSize(true)
        binding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSongs.adapter = songItemsAdapter

        viewModel.songs.observe(viewLifecycleOwner) {
            lifecycleScope.launch(Dispatchers.Default) {
                songItemsAdapter.submitCollection(it)
            }
        }
    }

    private suspend fun filterSongs() {
        Log.v(TAG, "filterSongs invoked")
        val title: String = binding.edSongTitleFilter.editableText.toString()
        val categoryId: String? = getSelectedCategoryId(binding.spnCategory)

        viewModel.resetSongSelection()
        viewModel.filterSongs(title, categoryId = categoryId)
    }

    private fun onPickSong(item: SongItem?) {
        item ?: return
        viewModel.resetPickedSong()
        viewModel.resetSongSelection()

        val intent = Intent(requireContext(), SongControlsActivity::class.java)
        intent.putExtra("songId", item.song.id)
        startActivity(intent)
    }

    private fun onSelectSong(numberOfSelectedSongs: Pair<Int, Int>): Boolean {
        val (countBefore: Int, countAfter: Int) = numberOfSelectedSongs

        if ((countBefore == 0 && countAfter == 1) || (countBefore == 1 && countAfter == 0)) {
            songItemsAdapter.notifyItemRangeChanged(0, viewModel.songs.value!!.size)
        }

        when (countAfter) {
            0 -> {
                actionMode?.finish()
                return false
            }
            1 -> {
                if (actionMode == null) {
                    actionMode = (requireActivity() as AppCompatActivity)
                        .startSupportActionMode(actionModeCallback)
                }

                showMenuActions()
            }
            2 -> showMenuActions(showEdit = false)
        }
        return true
    }

    private fun editSelectedSong(): Boolean {
        val selectedItem = viewModel.getSelectedSong()

        Log.v(TAG, "Editing song : ${selectedItem.song}")
        val intent = Intent(requireContext(), SongEditorActivity::class.java)
        intent.putExtra("songId", selectedItem.song.id)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun startExport(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        exportChooserResultLauncher.launch(chooserIntent)

        return true
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

            @Suppress("BlockingMethodInNonBlockingContext")
            requireActivity().contentResolver.openOutputStream(uri)!!
                .use { outputStream ->
                    viewModel.exportSelectedSongs(
                        requireActivity().cacheDir.canonicalPath,
                        outputStream,
                        dialogFragment.messageResourceId
                    )
                }

            dialogFragment.dismiss()
            songItemsAdapter.notifyItemRangeChanged(0, viewModel.songs.value!!.size)
        }
    }

    private fun addSetlist(): Boolean {
        val setlistSongs = viewModel.getSelectedSongIds().toTypedArray()
        val intent = Intent(context, SetlistEditorActivity::class.java)
        intent.putExtra("setlistSongs", setlistSongs)
        startActivity(intent)

        resetSelection()
        return true
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

    private fun getSelectedCategoryId(categorySpinner: Spinner): String? {
        categorySpinner.selectedItem ?: return null
        return (categorySpinner.selectedItem as CategoryItem).category.id
    }

    private fun resetSelection() {
        viewModel.resetSongSelection()
        songItemsAdapter.notifyItemRangeChanged(0, viewModel.songs.value!!.size)
    }

    private inner class SongsActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_main, menu)
            mode.title = ""
            actionMenu = menu
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            showMenuActions(showGroupActions = false, showEdit = false)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            lifecycleScope.launch(Dispatchers.Main) {
                val result = when (item.itemId) {
                    R.id.action_menu_delete -> {
                        viewModel.deleteSelectedSongs()
                        true
                    }
                    R.id.action_menu_export_selected -> startExport()
                    R.id.action_menu_edit -> editSelectedSong()
                    R.id.action_menu_add_setlist -> addSetlist()
                    else -> false
                }

                if (result) {
                    mode.finish()
                }
            }

            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            actionMenu = null
            resetSelection()
        }
    }

}
