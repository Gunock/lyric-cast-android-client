/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 1:21 PM
 */

package pl.gunock.lyriccast.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.SetlistControlsActivity
import pl.gunock.lyriccast.activities.SetlistEditorActivity
import pl.gunock.lyriccast.adapters.SetlistItemsAdapter
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.DatabaseViewModelFactory
import pl.gunock.lyriccast.datamodel.LyricCastRepository
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

    private lateinit var repository: LyricCastRepository
    private val databaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModelFactory(
            requireContext(),
            (requireActivity().application as LyricCastApplication).repository
        )
    }

    private lateinit var menu: Menu
    private lateinit var searchViewEditText: EditText
    private lateinit var setlistRecyclerView: RecyclerView

    private lateinit var setlistItemsAdapter: SetlistItemsAdapter
    private lateinit var selectionTracker: SelectionTracker<SetlistItemsAdapter.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        repository = (requireActivity().application as LyricCastApplication).repository
        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (selectionTracker.count > 0) {
                        setlistItemsAdapter.resetSelection()
                        resetSelection()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
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

        setlistRecyclerView = view.findViewById<RecyclerView>(R.id.rcv_setlists).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        setupListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)

        menu.findItem(R.id.menu_add_setlist).isVisible = false

        showMenuActions(showDelete = false, showEdit = false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> deleteSelectedSetlists()
            R.id.menu_export_selected -> startExport()
            R.id.menu_edit -> editSelectedSetlist()
            else -> super.onOptionsItemSelected(item)
        }
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

        setupSetlists()

        searchViewEditText.setText("")
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
        val dialogFragment = ProgressDialogFragment(getString(R.string.preparing_data))
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Light_Dialog
        )
        dialogFragment.show(activity.supportFragmentManager, ProgressDialogFragment.TAG)

        val message = dialogFragment.message
        CoroutineScope(Dispatchers.IO).launch {
            val exportDir = File(requireActivity().filesDir.canonicalPath, ".export")
            exportDir.deleteRecursively()
            exportDir.mkdirs()

            val selectedSongs = setlistItemsAdapter.setlistItems
                .filter { it.isSelected.value!! }

            val setlistNames: Set<String> = selectedSongs.map { it.setlist.name }.toSet()

            val exportData = databaseViewModel.getDatabaseTransferData()

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

            message.postValue(getString(R.string.export_saving_json))
            val songsString = JSONArray(songJsons).toString()
            val categoriesString = JSONArray(categoryJsons).toString()
            val setlistsString = JSONArray(setlistJsons).toString()
            File(exportDir, "songs.json").writeText(songsString)
            File(exportDir, "categories.json").writeText(categoriesString)
            File(exportDir, "setlists.json").writeText(setlistsString)

            message.postValue(getString(R.string.export_saving_zip))
            @Suppress("BlockingMethodInNonBlockingContext")
            FileHelper.zip(activity.contentResolver.openOutputStream(uri)!!, exportDir.path)

            message.postValue(getString(R.string.export_deleting_temp))
            exportDir.deleteRecursively()
            dialogFragment.dismiss()
        }

        resetSelection()

        return true
    }


    private fun setupSetlists() {
        val setlistRecyclerView = requireView().findViewById<RecyclerView>(R.id.rcv_setlists)
        selectionTracker = SelectionTracker(setlistRecyclerView, this::onSetlistClick)
        setlistItemsAdapter = SetlistItemsAdapter(
            requireContext(),
            selectionTracker = selectionTracker
        )

        setlistRecyclerView.adapter = setlistItemsAdapter

        databaseViewModel.allSetlists.observe(requireActivity()) { setlist ->
            setlistItemsAdapter.submitCollection(setlist ?: return@observe)
        }
    }

    private fun onSetlistClick(
        @Suppress("UNUSED_PARAMETER")
        holder: SetlistItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = setlistItemsAdapter.setlistItems[position]
        if (!isLongClick && selectionTracker.count == 0) {
            pickSetlist(item)
        } else {
            selectSetlist(item)
        }
        return true
    }

    private fun setupListeners() {
        searchViewEditText.addTextChangedListener(InputTextChangedListener { newText ->
            setlistItemsAdapter.filterItems(newText)
            resetSelection()
        })

        searchViewEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view)
            }
        }
    }

    private fun pickSetlist(item: SetlistItem) {
        val intent = Intent(context, SetlistControlsActivity::class.java)
        intent.putExtra("setlist", item.setlist)
        startActivity(intent)
    }

    private fun selectSetlist(
        item: SetlistItem
    ) {
        when (selectionTracker.countAfter) {
            0 -> {
                if (setlistItemsAdapter.showCheckBox.value!!) {
                    setlistItemsAdapter.showCheckBox.value = false
                }

                showMenuActions(showDelete = false, showEdit = false)
            }
            1 -> {
                if (!setlistItemsAdapter.showCheckBox.value!!) {
                    setlistItemsAdapter.showCheckBox.value = true
                }

                showMenuActions()
            }
            2 -> showMenuActions(showEdit = false)
        }

        item.isSelected.value = !item.isSelected.value!!
    }

    private fun editSelectedSetlist(): Boolean {
        val selectedItem = setlistItemsAdapter.setlistItems
            .first { setlistItem -> setlistItem.isSelected.value!! }

        val setlistWithSongs =
            runBlocking { repository.getSetlistWithSongs(selectedItem.setlist.id) }

        val intent = Intent(context, SetlistEditorActivity::class.java)
        intent.putExtra("setlistWithSongs", setlistWithSongs)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun deleteSelectedSetlists(): Boolean {
        val selectedSetlists = setlistItemsAdapter.setlistItems
            .filter { item -> item.isSelected.value!! }
            .map { item -> item.setlist.id }

        databaseViewModel.deleteSetlists(selectedSetlists)
        resetSelection()

        return true
    }

    private fun showMenuActions(showDelete: Boolean = true, showEdit: Boolean = true) {
        if (!this::menu.isInitialized) {
            return
        }

        menu.findItem(R.id.menu_delete).isVisible = showDelete
        menu.findItem(R.id.menu_edit).isVisible = showEdit
    }

    private fun resetSelection() {
        if (setlistItemsAdapter.showCheckBox.value!!) {
            setlistItemsAdapter.showCheckBox.value = false
        }

        selectionTracker.reset()
        showMenuActions(showDelete = false, showEdit = false)
    }

}
