/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 17:19
 */

package pl.gunock.lyriccast.ui.main.setlists

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.databinding.FragmentSetlistsBinding
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.shared.extensions.hideKeyboard
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.ui.main.SetlistItemsAdapter
import pl.gunock.lyriccast.ui.setlist_controls.SetlistControlsActivity
import pl.gunock.lyriccast.ui.setlist_editor.SetlistEditorActivity
import pl.gunock.lyriccast.ui.shared.fragments.ProgressDialogFragment
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener
import java.io.File

@AndroidEntryPoint
class SetlistsFragment : Fragment() {
    private val viewModel: SetlistsViewModel by activityViewModels()

    private lateinit var mBinding: FragmentSetlistsBinding

    private var mSetlistItemsAdapter: SetlistItemsAdapter? = null

    private var mToast: Toast? = null

    private var mActionMenu: Menu? = null
    private var mActionMode: ActionMode? = null
    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_main, menu)
            menu.findItem(R.id.action_menu_add_setlist).isVisible = false
            mode.title = ""
            mActionMenu = menu
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            showMenuActions(showGroupActions = false, showEdit = false)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val result = when (item.itemId) {
                R.id.action_menu_delete -> {
                    viewModel.deleteSelected()
                    resetSelection()
                    true
                }
                R.id.action_menu_export_selected -> startExport()
                R.id.action_menu_edit -> editSelectedSetlist()
                else -> false
            }

            if (result) {
                mode.finish()
            }

            return result
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode = null
            mActionMenu = null
            resetSelection()
        }
    }

    private val mExportChooserResultLauncher =
        registerForActivityResult(this::exportSelectedSetlists)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSetlistsBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pickedItem.observe(viewLifecycleOwner, this::onPickSetlist)
        viewModel.numberOfSelectedItems.observe(viewLifecycleOwner, this::onSelectSetlist)

        setupRecyclerView()
        setupListeners()
    }

    override fun onDestroyView() {
        mSetlistItemsAdapter!!.removeObservers()
        mSetlistItemsAdapter = null

        super.onDestroyView()
    }

    override fun onStop() {
        mActionMode?.finish()
        super.onStop()
    }

    private fun startExport(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        mExportChooserResultLauncher.launch(chooserIntent)

        return true
    }

    private fun exportSelectedSetlists(result: ActivityResult) =
        lifecycleScope.launch(Dispatchers.Main) {
            if (result.resultCode != Activity.RESULT_OK) {
                return@launch
            }

            val uri: Uri = result.data!!.data!!
            val activity = requireActivity()

            val dialogFragment =
                ProgressDialogFragment(getString(R.string.main_activity_export_preparing_data))
            dialogFragment.setStyle(
                DialogFragment.STYLE_NORMAL,
                R.style.Theme_LyricCast_Dialog
            )
            dialogFragment.show(activity.supportFragmentManager, ProgressDialogFragment.TAG)

            val exportData = viewModel.dataTransferRepository.getDatabaseTransferData()

            withContext(Dispatchers.IO) {
                val exportDir = File(requireActivity().cacheDir.canonicalPath, ".export")
                exportDir.deleteRecursively()
                exportDir.mkdirs()

                val selectedSongs = mSetlistItemsAdapter!!.setlistItems
                    .filter { it.isSelected.value!! }

                val setlistNames: Set<String> = selectedSongs.map { it.setlist.name }.toSet()


                val exportSetlists = exportData.setlistDtos!!
                    .filter { it.name in setlistNames }

                val songTitles: Set<String> = exportSetlists.flatMap { it.songs }.toSet()

                val categoryNames: Set<String> = exportData.songDtos!!
                    .filter { it.title in songTitles }
                    .mapNotNull { it.category }
                    .toSet()

                val songJsons: List<JSONObject> = exportData.songDtos!!
                    .filter { it.title in songTitles }
                    .map { it.toJson() }

                val categoryJsons = exportData.categoryDtos!!
                    .filter { it.name in categoryNames }
                    .map { it.toJson() }

                val setlistJsons = exportSetlists.filter { it.name in setlistNames }
                    .map { it.toJson() }

                dialogFragment.message = getString(R.string.main_activity_export_saving_json)
                val songsString = JSONArray(songJsons).toString()
                val categoriesString = JSONArray(categoryJsons).toString()
                val setlistsString = JSONArray(setlistJsons).toString()
                File(exportDir, "songs.json").writeText(songsString)
                File(exportDir, "categories.json").writeText(categoriesString)
                File(exportDir, "setlists.json").writeText(setlistsString)

                dialogFragment.message = getString(R.string.main_activity_export_saving_zip)
                @Suppress("BlockingMethodInNonBlockingContext")
                FileHelper.zip(activity.contentResolver.openOutputStream(uri)!!, exportDir.path)

                dialogFragment.message = getString(R.string.main_activity_export_deleting_temp)
                exportDir.deleteRecursively()
                dialogFragment.dismiss()
            }

            resetSelection()
        }

    private fun setupRecyclerView() {
        mSetlistItemsAdapter = SetlistItemsAdapter(
            requireContext(),
            selectionTracker = viewModel.selectionTracker
        )

        mBinding.rcvSetlists.setHasFixedSize(true)
        mBinding.rcvSetlists.layoutManager = LinearLayoutManager(requireContext())
        mBinding.rcvSetlists.adapter = mSetlistItemsAdapter

        viewModel.setlists.observe(viewLifecycleOwner) {
            lifecycleScope.launch(Dispatchers.Default) {
                mSetlistItemsAdapter?.submitCollection(it)
            }
        }
    }


    private fun setupListeners() {
        mBinding.edSetlistFilter.addTextChangedListener(InputTextChangedListener { newText ->
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.filter(newText)
                resetSelection()
            }
        })

        mBinding.edSetlistFilter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                view.hideKeyboard()
            }
        }
    }

    private fun onPickSetlist(item: SetlistItem): Boolean {
        if (item.setlist.presentation.isEmpty()) {
            mToast?.cancel()
            mToast = Toast.makeText(
                requireContext(),
                getString(R.string.main_activity_setlist_is_empty),
                Toast.LENGTH_SHORT
            )
            mToast!!.show()
            requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return false
        }

        val intent = Intent(context, SetlistControlsActivity::class.java)
        intent.putExtra("setlistId", item.setlist.id)
        startActivity(intent)
        return true
    }

    private fun onSelectSetlist(numberOfSelectedItems: Int): Boolean {
        when (numberOfSelectedItems) {
            0 -> {
                mActionMode?.finish()
                return false
            }
            1 -> {
                if (!mSetlistItemsAdapter!!.showCheckBox.value!!) {
                    mSetlistItemsAdapter!!.showCheckBox.postValue(true)
                }

                if (mActionMode == null) {
                    mActionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(
                        mActionModeCallback
                    )
                }

                showMenuActions()
            }
            2 -> showMenuActions(showEdit = false)
        }

        return true
    }

    private fun editSelectedSetlist(): Boolean {
        val selectedItem = mSetlistItemsAdapter!!.setlistItems
            .first { setlistItem -> setlistItem.isSelected.value!! }

        val intent = Intent(context, SetlistEditorActivity::class.java)
        intent.putExtra("setlistId", selectedItem.setlist.id)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun showMenuActions(
        showGroupActions: Boolean = true,
        showEdit: Boolean = true
    ) {
        mActionMenu ?: return
        mActionMenu!!.findItem(R.id.action_menu_delete).isVisible = showGroupActions
        mActionMenu!!.findItem(R.id.action_menu_export_selected).isVisible = showGroupActions
        mActionMenu!!.findItem(R.id.action_menu_edit).isVisible = showEdit
    }

    private fun resetSelection() {
        if (mSetlistItemsAdapter!!.showCheckBox.value!!) {
            mSetlistItemsAdapter!!.showCheckBox.postValue(false)
        }

        viewModel.resetSelection()
    }

}
