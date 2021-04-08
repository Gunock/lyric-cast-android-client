/*
 * Created by Tomasz Kiljanczyk on 4/8/21 1:47 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/8/21 1:41 PM
 */

package pl.gunock.lyriccast.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.activities.SetlistEditorActivity
import pl.gunock.lyriccast.activities.SongControlsActivity
import pl.gunock.lyriccast.activities.SongEditorActivity
import pl.gunock.lyriccast.adapters.SongItemsAdapter
import pl.gunock.lyriccast.adapters.spinner.CategorySpinnerAdapter
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.DatabaseViewModelFactory
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.SetlistSongCrossRef
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.fragments.dialogs.ProgressDialogFragment
import pl.gunock.lyriccast.helpers.KeyboardHelper
import pl.gunock.lyriccast.listeners.InputTextChangedListener
import pl.gunock.lyriccast.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.misc.SelectionTracker
import pl.gunock.lyriccast.models.SongItem
import java.io.File


class SongsFragment : Fragment() {
    private companion object {
        const val TAG = "SongsFragment"

        const val EXPORT_SELECTED_RESULT_CODE = 3
    }

    private var mCastContext: CastContext? = null
    private lateinit var mRepository: LyricCastRepository
    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModelFactory(
            requireContext(),
            (requireActivity().application as LyricCastApplication).repository
        )
    }

    private lateinit var mSearchViewEditText: EditText
    private lateinit var mCategorySpinner: Spinner
    private lateinit var mSongItemsRecyclerView: RecyclerView

    private lateinit var mSongItemsAdapter: SongItemsAdapter
    private lateinit var mSelectionTracker: SelectionTracker<SongItemsAdapter.ViewHolder>

    private var mActionMenu: Menu? = null
    private var mActionMode: ActionMode? = null
    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.action_menu_main, menu)
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
                R.id.action_menu_delete -> deleteSelectedSongs()
                R.id.action_menu_export_selected -> startExport()
                R.id.action_menu_edit -> editSelectedSong()
                R.id.action_menu_add_setlist -> addSetlist()
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
        super.onCreate(savedInstanceState)

        mRepository = (requireActivity().application as LyricCastApplication).repository
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCastContext = CastContext.getSharedInstance()

        val searchView: TextInputLayout = view.findViewById(R.id.tv_filter_songs)
        mSearchViewEditText = searchView.editText!!

        mCategorySpinner = view.findViewById(R.id.spn_category)
        mSongItemsRecyclerView = view.findViewById(R.id.rcv_songs)

        view.findViewById<SwitchCompat>(R.id.swt_selected_songs).visibility = View.GONE

        mSongItemsRecyclerView.setHasFixedSize(true)
        mSongItemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupCategorySpinner()
        setupSongs()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()

        resetSelection()
        mSearchViewEditText.setText("")
    }

    override fun onStop() {
        mActionMode?.finish()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val uri: Uri = data?.data!!
        when (requestCode) {
            EXPORT_SELECTED_RESULT_CODE -> exportSelectedSongs(uri)
        }
    }

    private fun setupListeners() {
        mSearchViewEditText.addTextChangedListener(InputTextChangedListener {
            filterSongs(
                mSearchViewEditText.editableText.toString(),
                getSelectedCategoryId()
            )
        })

        mSearchViewEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                KeyboardHelper.hideKeyboard(view)
            }
        }

        mCategorySpinner.onItemSelectedListener = ItemSelectedSpinnerListener { _, _ ->
            filterSongs(
                mSearchViewEditText.editableText.toString(),
                getSelectedCategoryId()
            )
        }
    }

    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(requireContext())
        mCategorySpinner.adapter = categorySpinnerAdapter

        mDatabaseViewModel.allCategories.observe(requireActivity()) { categories ->
            categorySpinnerAdapter.submitCollection(categories)
            mCategorySpinner.setSelection(0)
        }
    }

    private fun setupSongs() {
        mSelectionTracker = SelectionTracker(mSongItemsRecyclerView, this::onSongClick)
        mSongItemsAdapter = SongItemsAdapter(
            requireContext(),
            mSelectionTracker = mSelectionTracker
        )
        mSongItemsRecyclerView.adapter = mSongItemsAdapter

        mDatabaseViewModel.allSongs.observe(requireActivity(), { songs ->
            mSongItemsAdapter.submitCollection(songs ?: return@observe)
        })
    }

    private fun onSongClick(
        @Suppress("UNUSED_PARAMETER")
        holder: SongItemsAdapter.ViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = mSongItemsAdapter.songItems[position]

        if (!isLongClick && mSelectionTracker.count == 0) {
            pickSong(item)
        } else {
            return selectSong(item)
        }

        return true
    }

    private fun filterSongs(
        title: String,
        categoryId: Long = Long.MIN_VALUE
    ) {
        Log.v(TAG, "filterSongs invoked")

        mSongItemsAdapter.filterItems(title, categoryId = categoryId)
        resetSelection()
    }

    private fun pickSong(item: SongItem) {
        val songWithLyrics = runBlocking { mRepository.getSongWithLyrics(item.song.id) }!!

        val lyricsTextMap = songWithLyrics.lyricsSectionsToTextMap()
        val lyrics: List<String> = songWithLyrics.crossRef
            .sorted()
            .map { lyricsTextMap[it.lyricsSectionId]!! }

        resetSelection()

        val intent = Intent(requireContext(), SongControlsActivity::class.java)
        intent.putExtra("lyrics", lyrics.toTypedArray())
        startActivity(intent)
    }

    private fun selectSong(item: SongItem): Boolean {
        item.isSelected.value = !item.isSelected.value!!

        when (mSelectionTracker.countAfter) {
            0 -> {
                mActionMode?.finish()
                return false
            }
            1 -> {
                if (!mSongItemsAdapter.showCheckBox.value!!) {
                    mSongItemsAdapter.showCheckBox.value = true
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


    private fun editSelectedSong(): Boolean {
        val selectedItem =
            mSongItemsAdapter.songItems.first { songItem -> songItem.isSelected.value!! }
        Log.v(TAG, "Editing song : ${selectedItem.song}")
        val intent = Intent(requireContext(), SongEditorActivity::class.java)
        intent.putExtra("song", selectedItem.song)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun startExport(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        startActivityForResult(chooserIntent, EXPORT_SELECTED_RESULT_CODE)

        return true
    }

    private fun exportSelectedSongs(uri: Uri): Boolean {
        val activity = requireActivity()
        val dialogFragment =
            ProgressDialogFragment(getString(R.string.main_activity_export_preparing_data))
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog
        )
        dialogFragment.show(activity.supportFragmentManager, ProgressDialogFragment.TAG)

        CoroutineScope(Dispatchers.IO).launch {
            val exportDir = File(requireActivity().filesDir.canonicalPath, ".export")
            exportDir.deleteRecursively()
            exportDir.mkdirs()

            val selectedSongs = mSongItemsAdapter.songItems
                .filter { it.isSelected.value!! }

            val songTitles: Set<String> = selectedSongs.map { it.song.title }.toSet()
            val categoryNames: Set<String> = selectedSongs.mapNotNull { it.category?.name }.toSet()

            val exportData = mDatabaseViewModel.getDatabaseTransferData()
            val songJsons = exportData.songDtos!!
                .filter { it.title in songTitles }
                .map { it.toJson() }

            val categoryJsons = exportData.categoryDtos!!
                .filter { it.name in categoryNames }
                .map { it.toJson() }

            dialogFragment.message = getString(R.string.main_activity_export_saving_json)
            val songsString = JSONArray(songJsons).toString()
            val categoriesString = JSONArray(categoryJsons).toString()
            File(exportDir, "songs.json").writeText(songsString)
            File(exportDir, "categories.json").writeText(categoriesString)

            dialogFragment.message = getString(R.string.main_activity_export_saving_zip)
            @Suppress("BlockingMethodInNonBlockingContext")
            FileHelper.zip(activity.contentResolver.openOutputStream(uri)!!, exportDir.path)

            dialogFragment.message = getString(R.string.main_activity_export_deleting_temp)
            exportDir.deleteRecursively()
            dialogFragment.dismiss()
        }

        resetSelection()

        return true
    }

    private fun addSetlist(): Boolean {
        val setlist = Setlist(null, "")
        val songs = mSongItemsAdapter.songItems
            .filter { it.isSelected.value == true }
            .map { item -> item.song }

        val crossRef: List<SetlistSongCrossRef> = mSongItemsAdapter.songItems
            .mapIndexed { index, item ->
                SetlistSongCrossRef(null, setlist.id, item.song.id, index)
            }

        val setlistWithSongs = SetlistWithSongs(setlist, songs, crossRef)

        val intent = Intent(context, SetlistEditorActivity::class.java)
        intent.putExtra("setlistWithSongs", setlistWithSongs)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun deleteSelectedSongs(): Boolean {
        val selectedSongs = mSongItemsAdapter.songItems
            .filter { item -> item.isSelected.value!! }
            .map { item -> item.song.id }

        mDatabaseViewModel.deleteSongs(selectedSongs)

        resetSelection()

        return true
    }

    private fun resetSelection() {
        if (mSongItemsAdapter.showCheckBox.value!!) {
            mSongItemsAdapter.showCheckBox.value = false
        }

        mSongItemsAdapter.resetSelection()
    }

    private fun showMenuActions(
        showGroupActions: Boolean = true,
        showEdit: Boolean = true
    ) {
        mActionMenu ?: return
        mActionMenu!!.findItem(R.id.action_menu_delete).isVisible = showGroupActions
        mActionMenu!!.findItem(R.id.action_menu_export_selected).isVisible = showGroupActions
        mActionMenu!!.findItem(R.id.action_menu_add_setlist).isVisible = showGroupActions
        mActionMenu!!.findItem(R.id.action_menu_edit).isVisible = showEdit
    }

    private fun getSelectedCategoryId(): Long {
        return ((mCategorySpinner.selectedItem ?: Category.ALL) as Category).id
    }
}
