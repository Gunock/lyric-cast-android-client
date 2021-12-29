/*
 * Created by Tomasz Kiljanczyk on 29/12/2021, 15:31
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 29/12/2021, 15:28
 */

package pl.gunock.lyriccast.ui.main.setlists

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
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
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.FragmentSetlistsBinding
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.shared.extensions.hideKeyboard
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.shared.utils.DialogFragmentUtils
import pl.gunock.lyriccast.ui.setlist_controls.SetlistControlsActivity
import pl.gunock.lyriccast.ui.setlist_editor.SetlistEditorActivity
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener

@AndroidEntryPoint
class SetlistsFragment : Fragment() {
    private companion object {
        const val TAG = "SetlistsFragment"
    }

    private val viewModel: SetlistsModel by activityViewModels()

    private lateinit var binding: FragmentSetlistsBinding

    private lateinit var setlistItemsAdapter: SetlistItemsAdapter

    private var toast: Toast? = null

    private var actionMenu: Menu? = null
    private var actionMode: ActionMode? = null
    private val actionModeCallback: ActionMode.Callback = SetlistsActionModeCallback()

    private val exportChooserResultLauncher =
        registerForActivityResult(this::exportSelectedSetlists)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetlistsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pickedSetlist.observe(viewLifecycleOwner, this::onPickSetlist)
        viewModel.numberOfSelectedSetlists.observe(viewLifecycleOwner, this::onSelectSetlist)
        viewModel.selectedSetlistPosition.observe(viewLifecycleOwner) {
            setlistItemsAdapter.notifyItemChanged(it)
            binding.tinSetlistNameFilter.clearFocus()
        }

        setupRecyclerView()
        setupListeners()
    }

    override fun onStop() {
        actionMode?.finish()
        viewModel.resetSetlistSelection()
        super.onStop()
    }

    private fun startExport(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        exportChooserResultLauncher.launch(chooserIntent)

        return true
    }

    private fun exportSelectedSetlists(result: ActivityResult) {
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
                    viewModel.exportSelectedSetlists(
                        requireActivity().cacheDir.canonicalPath,
                        outputStream,
                        dialogFragment.messageResourceId
                    )
                }

            dialogFragment.dismiss()
            setlistItemsAdapter.notifyItemRangeChanged(0, viewModel.setlists.value!!.size)
        }
    }

    private fun setupRecyclerView() {
        setlistItemsAdapter = SetlistItemsAdapter(viewModel.selectionTracker)

        binding.rcvSetlists.setHasFixedSize(true)
        binding.rcvSetlists.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvSetlists.adapter = setlistItemsAdapter

        viewModel.setlists.observe(viewLifecycleOwner) {
            setlistItemsAdapter.submitCollection(it)
        }
    }


    private fun setupListeners() {
        binding.edSetlistNameFilter.addTextChangedListener(InputTextChangedListener { newText ->
            lifecycleScope.launch(Dispatchers.Default) {
                viewModel.resetSetlistSelection()
                viewModel.filterSetlists(newText)
            }
        })

        binding.edSetlistNameFilter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                view.hideKeyboard()
            }
        }
    }

    private fun onPickSetlist(item: SetlistItem?) {
        item ?: return
        viewModel.resetPickedSetlist()
        viewModel.resetSetlistSelection()

        if (item.setlist.presentation.isEmpty()) {
            toast?.cancel()
            toast = Toast.makeText(
                requireContext(),
                getString(R.string.main_activity_setlist_is_empty),
                Toast.LENGTH_SHORT
            )
            toast!!.show()
            requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return
        }

        val intent = Intent(context, SetlistControlsActivity::class.java)
        intent.putExtra("setlistId", item.setlist.id)
        startActivity(intent)
    }

    private fun onSelectSetlist(numberOfSelectedSetlists: Pair<Int, Int>): Boolean {
        val (countBefore: Int, countAfter: Int) = numberOfSelectedSetlists

        if ((countBefore == 0 && countAfter == 1) || (countBefore == 1 && countAfter == 0)) {
            setlistItemsAdapter.notifyItemRangeChanged(0, viewModel.setlists.value!!.size)
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

    private fun editSelectedSetlist(): Boolean {
        val selectedItem = viewModel.setlists.value!!
            .first { setlistItem -> setlistItem.isSelected }

        Log.v(TAG, "Editing setlist : ${selectedItem.setlist}")
        val intent = Intent(context, SetlistEditorActivity::class.java)
        intent.putExtra("setlistId", selectedItem.setlist.id)
        startActivity(intent)

        viewModel.resetSetlistSelection()

        return true
    }

    private fun showMenuActions(
        showGroupActions: Boolean = true,
        showEdit: Boolean = true
    ) {
        actionMenu?.apply {
            findItem(R.id.action_menu_delete).isVisible = showGroupActions
            findItem(R.id.action_menu_export_selected).isVisible = showGroupActions
            findItem(R.id.action_menu_edit).isVisible = showEdit
        }
    }

    private inner class SetlistsActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_main, menu)
            menu.findItem(R.id.action_menu_add_setlist).isVisible = false
            mode.title = ""
            actionMenu = menu
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
                        viewModel.deleteSelectedSetlists()
                        true
                    }
                    R.id.action_menu_export_selected -> startExport()
                    R.id.action_menu_edit -> editSelectedSetlist()
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
            viewModel.resetSetlistSelection()
        }
    }

}
