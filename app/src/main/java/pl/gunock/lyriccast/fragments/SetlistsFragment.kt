/*
 * Created by Tomasz Kiljanczyk on 4/24/21 4:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/24/21 4:28 PM
 */

package pl.gunock.lyriccast.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.SetlistControlsActivity
import pl.gunock.lyriccast.activities.SetlistEditorActivity
import pl.gunock.lyriccast.adapters.SetlistItemsAdapter
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.fragments.dialogs.ProgressDialogFragment
import pl.gunock.lyriccast.helpers.KeyboardHelper
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.SetlistItem
import java.io.File


class SetlistsFragment : Fragment() {
    private companion object {
        const val EXPORT_SELECTED_RESULT_CODE = 4
    }

    private lateinit var mDatabaseViewModel: DatabaseViewModel

    private var mSetlistItemsAdapter: SetlistItemsAdapter? = null
    private lateinit var mSelectionTracker: SelectionTracker<SetlistItemsAdapter.ViewHolder>

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
                R.id.action_menu_delete -> deleteSelectedSetlists()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        mDatabaseViewModel = DatabaseViewModel.Factory(resources).create()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
    }

    override fun onDestroyView() {
        mSetlistItemsAdapter!!.removeObservers()
        mSetlistItemsAdapter = null

        mDatabaseViewModel.close()

        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val uri: Uri = data?.data!!
        when (requestCode) {
            EXPORT_SELECTED_RESULT_CODE -> exportSelectedSetlists(uri)
        }
    }

    override fun onResume() {
        super.onResume()

        resetSelection()
        requireView().findViewById<EditText>(R.id.tin_setlist_filter).setText("")
    }

    override fun onStop() {
        mActionMode?.finish()
        super.onStop()
    }

    private fun startExport(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        startActivityForResult(chooserIntent, EXPORT_SELECTED_RESULT_CODE)

        return true
    }

    private fun exportSelectedSetlists(uri: Uri): Boolean {
        val activity = requireActivity()
        val dialogFragment =
            ProgressDialogFragment(getString(R.string.main_activity_export_preparing_data))
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog
        )
        dialogFragment.show(activity.supportFragmentManager, ProgressDialogFragment.TAG)

        val exportData = mDatabaseViewModel.getDatabaseTransferData()

        CoroutineScope(Dispatchers.IO).launch {
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
            FileHelper.zip(activity.contentResolver, uri, exportDir.path)

            dialogFragment.message = getString(R.string.main_activity_export_deleting_temp)
            exportDir.deleteRecursively()
            dialogFragment.dismiss()
        }

        resetSelection()

        return true
    }

    private fun setupRecyclerView() {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.rcv_setlists)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mSelectionTracker = SelectionTracker(this::onSetlistClick)
        mSetlistItemsAdapter = SetlistItemsAdapter(
            requireContext(),
            selectionTracker = mSelectionTracker
        )

        recyclerView.adapter = mSetlistItemsAdapter

        mDatabaseViewModel.allSetlists.addChangeListener { setlists ->
            mSetlistItemsAdapter?.submitCollection(setlists)
        }
    }

    private fun onSetlistClick(
        @Suppress("UNUSED_PARAMETER")
        holder: SetlistItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = mSetlistItemsAdapter!!.setlistItems[position]
        if (!isLongClick && mSelectionTracker.count == 0) {
            pickSetlist(item)
        } else {
            return selectSetlist(item)
        }
        return true
    }

    private fun setupListeners() {
        val filterEditText: EditText = requireView().findViewById(R.id.tin_setlist_filter)

        filterEditText.addTextChangedListener(InputTextChangedListener { newText ->
            mSetlistItemsAdapter!!.filterItems(newText)
            resetSelection()
        })

        filterEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view)
            }
        }
    }

    private fun pickSetlist(item: SetlistItem) {
        val intent = Intent(context, SetlistControlsActivity::class.java)
        intent.putExtra("setlistId", item.setlist.id)
        startActivity(intent)
    }

    private fun selectSetlist(item: SetlistItem): Boolean {
        item.isSelected.value = !item.isSelected.value!!
        when (mSelectionTracker.countAfter) {
            0 -> {
                mActionMode?.finish()
                return false
            }
            1 -> {
                if (!mSetlistItemsAdapter!!.showCheckBox.value!!) {
                    mSetlistItemsAdapter!!.showCheckBox.value = true
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

    private fun deleteSelectedSetlists(): Boolean {
        val selectedSetlists = mSetlistItemsAdapter!!.setlistItems
            .filter { item -> item.isSelected.value!! }
            .map { item -> item.setlist.id }

        mDatabaseViewModel.deleteSetlists(selectedSetlists)
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
            mSetlistItemsAdapter!!.showCheckBox.value = false
        }

        mSetlistItemsAdapter!!.resetSelection()
    }

}
