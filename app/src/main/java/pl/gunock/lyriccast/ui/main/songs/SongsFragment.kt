/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 17:19
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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.databinding.FragmentSongsBinding
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.shared.extensions.hideKeyboard
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.ui.setlist_editor.SetlistEditorActivity
import pl.gunock.lyriccast.ui.shared.adapters.CategorySpinnerAdapter
import pl.gunock.lyriccast.ui.shared.adapters.SongItemsAdapter
import pl.gunock.lyriccast.ui.shared.fragments.ProgressDialogFragment
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedSpinnerListener
import pl.gunock.lyriccast.ui.song_controls.SongControlsActivity
import pl.gunock.lyriccast.ui.song_editor.SongEditorActivity
import java.io.File
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class SongsFragment : Fragment() {
    private companion object {
        const val TAG = "SongsFragment"
    }

    private val viewModel: SongsViewModel by activityViewModels()

    @Inject
    lateinit var dataTransferRepository: DataTransferRepository

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    private var mSongItemsAdapter: SongItemsAdapter? = null
    private lateinit var mBinding: FragmentSongsBinding

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
                R.id.action_menu_delete -> {
                    viewModel.deleteSelected()
                    resetSelection()
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

            return result
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode = null
            mActionMenu = null
            resetSelection()
        }
    }

    private val mExportChooserResultLauncher = registerForActivityResult(this::exportSelectedSongs)

    //    private var mSongsSubscription: Disposable? = null
    private var mCategoriesSubscription: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSongsBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.swtSelectedSongs.visibility = View.GONE

        viewModel.pickedItem.observe(viewLifecycleOwner, this::onPickSong)
        viewModel.numberOfSelectedItems.observe(viewLifecycleOwner, this::onSelectSong)

        setupCategorySpinner()
        setupRecyclerView()
        setupListeners()
    }

    override fun onDestroyView() {
        mSongItemsAdapter!!.removeObservers()
        mSongItemsAdapter = null
        mBinding.spnCategory.adapter = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()

        mBinding.edSongFilter.setText("")

        mCategoriesSubscription =
            categoriesRepository.getAllCategories().subscribe { categories: List<Category> ->
                lifecycleScope.launch(Dispatchers.Default) {
                    val categorySpinnerAdapter =
                        mBinding.spnCategory.adapter as CategorySpinnerAdapter
                    categorySpinnerAdapter.submitCollection(categories)
                }
            }
    }

    override fun onPause() {
        super.onPause()

        mCategoriesSubscription?.dispose()
        mCategoriesSubscription = null
    }

    override fun onStop() {
        mActionMode?.finish()
        super.onStop()
    }

    private fun setupListeners() {
        mBinding.edSongFilter.addTextChangedListener(InputTextChangedListener {
            lifecycleScope.launch(Dispatchers.Default) {
                filterSongs()
            }
        })

        mBinding.edSongFilter.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                view.hideKeyboard()
            }
        }

        mBinding.spnCategory.onItemSelectedListener =
            ItemSelectedSpinnerListener { _, _ ->
                lifecycleScope.launch(Dispatchers.Default) {
                    filterSongs()
                }
            }
    }

    @SuppressLint("CutPasteId")
    private fun setupCategorySpinner() {
        val categorySpinnerAdapter = CategorySpinnerAdapter(requireContext())

        mBinding.spnCategory.adapter = categorySpinnerAdapter
    }

    private fun setupRecyclerView() {
        mSongItemsAdapter = SongItemsAdapter(
            requireContext(),
            selectionTracker = viewModel.selectionTracker
        )

        mBinding.rcvSongs.setHasFixedSize(true)
        mBinding.rcvSongs.layoutManager = LinearLayoutManager(requireContext())
        mBinding.rcvSongs.adapter = mSongItemsAdapter

        viewModel.songs.observe(viewLifecycleOwner) {
            lifecycleScope.launch(Dispatchers.Default) {
                mSongItemsAdapter?.submitCollection(it)
            }
        }
    }

    private suspend fun filterSongs() {
        Log.v(TAG, "filterSongs invoked")
        val title: String = mBinding.edSongFilter.editableText.toString()
        val categoryId: String? = getSelectedCategoryId(mBinding.spnCategory)

        viewModel.filter(title, categoryId = categoryId)
        resetSelection()
    }

    private fun onPickSong(item: SongItem) {
        resetSelection()

        val intent = Intent(requireContext(), SongControlsActivity::class.java)
        intent.putExtra("songId", item.song.id)
        startActivity(intent)
    }

    private fun onSelectSong(numberOfSelectedItems: Int): Boolean {
        when (numberOfSelectedItems) {
            0 -> {
                mActionMode?.finish()
                return false
            }
            1 -> {
                if (!mSongItemsAdapter!!.showCheckBox.value!!) {
                    mSongItemsAdapter!!.showCheckBox.postValue(true)
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
            mSongItemsAdapter!!.songItems.first { songItem -> songItem.isSelected.value!! }

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
        mExportChooserResultLauncher.launch(chooserIntent)

        return true
    }

    private fun exportSelectedSongs(result: ActivityResult) =
        lifecycleScope.launch(Dispatchers.Default) {
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

            val exportData = dataTransferRepository.getDatabaseTransferData()
            withContext(Dispatchers.IO) {
                val exportDir = File(requireActivity().cacheDir.canonicalPath, ".export")
                exportDir.deleteRecursively()
                exportDir.mkdirs()

                val selectedSongs = mSongItemsAdapter!!.songItems
                    .filter { it.isSelected.value!! }

                val songTitles: Set<String> = selectedSongs.map { it.song.title }.toSet()
                val categoryNames: Set<String> =
                    selectedSongs.mapNotNull { it.song.category?.name }.toSet()


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
        }

    private fun addSetlist(): Boolean {
        val setlistSongs = mSongItemsAdapter!!.songItems
            .filter { it.isSelected.value == true }
            .map { item -> item.song.id }
            .toTypedArray()

        val intent = Intent(context, SetlistEditorActivity::class.java)
        intent.putExtra("setlistSongs", setlistSongs)
        startActivity(intent)

        resetSelection()

        return true
    }

    private fun resetSelection() {
        if (mSongItemsAdapter!!.showCheckBox.value!!) {
            mSongItemsAdapter!!.showCheckBox.postValue(false)
        }

        viewModel.resetSelection()
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

    private fun getSelectedCategoryId(categorySpinner: Spinner): String? {
        categorySpinner.selectedItem ?: return null

        return (categorySpinner.selectedItem as Category).id
    }
}
