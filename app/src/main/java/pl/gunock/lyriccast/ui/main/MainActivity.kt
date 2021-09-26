/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 17:19
 */

package pl.gunock.lyriccast.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.databinding.ActivityMainBinding
import pl.gunock.lyriccast.databinding.ContentMainBinding
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat
import pl.gunock.lyriccast.datatransfer.enums.SongXmlParserType
import pl.gunock.lyriccast.datatransfer.extensions.toJSONObjectList
import pl.gunock.lyriccast.datatransfer.factories.ImportSongXmlParserFactory
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SetlistDto
import pl.gunock.lyriccast.datatransfer.models.SongDto
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import pl.gunock.lyriccast.ui.main.setlists.SetlistsFragment
import pl.gunock.lyriccast.ui.setlist_editor.SetlistEditorActivity
import pl.gunock.lyriccast.ui.settings.SettingsActivity
import pl.gunock.lyriccast.ui.shared.fragments.ProgressDialogFragment
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedTabListener
import pl.gunock.lyriccast.ui.song_editor.SongEditorActivity
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"
    }

    @Inject
    lateinit var dataTransferRepository: DataTransferRepository

    private lateinit var mImportDialogViewModel: ImportDialogViewModel
    private lateinit var mBinding: ContentMainBinding

    private val mExportChooserResultLauncher = registerForActivityResult(this::exportAll)
    private val mImportChooserResultLauncher = registerForActivityResult(this::import)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarMain)
        mBinding = rootBinding.contentMain

        mImportDialogViewModel = ViewModelProvider(this).get(ImportDialogViewModel::class.java)

        mBinding.cstlFabContainer.visibility = View.GONE

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navh_main) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.first()
        if (currentFragment is SetlistsFragment) {
            mBinding.tblMainFragments.getTabAt(1)?.select()
        }

        setupListeners()
    }

    override fun onResume() {
        mBinding.advMain.loadAd()

        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider

        castActionProvider.routeSelector = CastContext.getSharedInstance()!!.mergedSelector!!

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_category -> goToCategoryManager()
            R.id.menu_settings -> goToSettings()
            R.id.menu_import_songs -> showImportDialog()
            R.id.menu_export_all -> startExport()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        mBinding.tblMainFragments.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                tab ?: return@ItemSelectedTabListener

                mBinding.fabAdd.clearFocus()

                val navController = findNavController(R.id.navh_main)
                if (tab.text == getString(R.string.title_songs)) {
                    Log.d(TAG, "Switching to song list")
                    navController.navigate(R.id.action_Setlists_to_Songs)
                } else if (tab.text == getString(R.string.title_setlists)) {
                    Log.d(TAG, "Switching to setlists")
                    navController.navigate(R.id.action_Songs_to_Setlists)
                }
            })

        val fabContainer = mBinding.cstlFabContainer
        mBinding.fabAdd.setOnClickListener {
            if (fabContainer.isVisible) {
                fabContainer.visibility = View.GONE
                mBinding.fabAdd.clearFocus()
            } else {
                fabContainer.visibility = View.VISIBLE
                mBinding.fabAdd.requestFocus()
            }
        }
        mBinding.fabAdd.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                fabContainer.visibility = View.VISIBLE
            } else {
                fabContainer.visibility = View.GONE
            }
        }

        mBinding.fabAddSetlist.setOnClickListener {
            val intent = Intent(baseContext, SetlistEditorActivity::class.java)
            startActivity(intent)
            mBinding.fabAdd.clearFocus()
        }

        mBinding.fabAddSong.setOnClickListener {
            val intent = Intent(baseContext, SongEditorActivity::class.java)
            startActivity(intent)
            mBinding.fabAdd.clearFocus()
        }
    }

    private fun startExport(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        mExportChooserResultLauncher.launch(chooserIntent)

        return true
    }

    private fun import(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }

        val uri: Uri = result.data!!.data!!

        Log.d(TAG, "Handling import result")
        Log.d(TAG, "Import parameters $mImportDialogViewModel")
        Log.d(TAG, "Selected file URI: $uri")
        if (mImportDialogViewModel.importFormat == ImportFormat.OPEN_SONG) {
            lifecycleScope.launch(Dispatchers.Main) { importOpenSong(uri) }
        } else if (mImportDialogViewModel.importFormat == ImportFormat.LYRIC_CAST) {
            lifecycleScope.launch(Dispatchers.Main) { importLyricCast(uri) }
        }
    }

    private fun exportAll(result: ActivityResult) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (result.resultCode != Activity.RESULT_OK) {
                return@launch
            }

            val uri: Uri = result.data!!.data!!

            val dialogFragment =
                ProgressDialogFragment(getString(R.string.main_activity_export_preparing_data))
            dialogFragment.setStyle(
                DialogFragment.STYLE_NORMAL,
                R.style.Theme_LyricCast_Dialog
            )
            dialogFragment.show(supportFragmentManager, ProgressDialogFragment.TAG)

            val exportData = dataTransferRepository.getDatabaseTransferData()

            withContext(Dispatchers.IO) {
                val exportDir = File(cacheDir.canonicalPath, ".export")
                exportDir.deleteRecursively()
                exportDir.mkdirs()


                dialogFragment.message = getString(R.string.main_activity_export_saving_json)
                val songsString = JSONArray(exportData.songDtos!!.map { it.toJson() }).toString()
                val categoriesString =
                    JSONArray(exportData.categoryDtos!!.map { it.toJson() }).toString()
                val setlistsString =
                    JSONArray(exportData.setlistDtos!!.map { it.toJson() }).toString()

                File(exportDir, "songs.json").writeText(songsString)
                File(exportDir, "categories.json").writeText(categoriesString)
                File(exportDir, "setlists.json").writeText(setlistsString)

                dialogFragment.message = getString(R.string.main_activity_export_saving_zip)
                @Suppress("BlockingMethodInNonBlockingContext")
                FileHelper.zip(contentResolver.openOutputStream(uri)!!, exportDir.path)

                dialogFragment.message = getString(R.string.main_activity_export_deleting_temp)
                exportDir.deleteRecursively()
                dialogFragment.dismiss()
            }
        }
    }

    private fun showImportDialog(): Boolean {
        val dialogFragment = ImportDialogFragment()
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog_NoTitle
        )

        mImportDialogViewModel.accepted.postValue(false)
        mImportDialogViewModel.accepted.observe(this) { if (it) startImport() }
        dialogFragment.show(supportFragmentManager, ImportDialogFragment.TAG)
        return true
    }

    private suspend fun importLyricCast(uri: Uri) {
        val dialogFragment = ProgressDialogFragment(getString(R.string.main_activity_loading_file))
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog
        )
        dialogFragment.show(supportFragmentManager, ProgressDialogFragment.TAG)

        val transferData: DatabaseTransferData? = withContext(Dispatchers.IO) {
            getImportData(uri)
        }

        if (transferData == null) {
            dialogFragment.message = getString(R.string.main_activity_import_incorrect_file_format)
            dialogFragment.setErrorColor(true)
            dialogFragment.setShowOkButton(true)
            return
        }

        val importOptions = ImportOptions(
            mImportDialogViewModel.deleteAll,
            mImportDialogViewModel.replaceOnConflict
        )

        dataTransferRepository.importSongs(
            transferData,
            dialogFragment.messageLiveData,
            importOptions
        )

        dialogFragment.dismiss()
    }

    private fun getImportData(uri: Uri): DatabaseTransferData? {
        val importDir = File(cacheDir.path, ".import")
        importDir.deleteRecursively()
        importDir.mkdirs()

        contentResolver.openInputStream(uri).use { inputStream ->
            FileHelper.unzip(inputStream!!, importDir.path)
        }

        try {
            val songsJson = JSONArray(File(importDir, "songs.json").readText())
            val categoriesJson = JSONArray(File(importDir, "categories.json").readText())

            val setlistsFile = File(importDir, "setlists.json")
            val setlistsJson: JSONArray? = if (setlistsFile.exists()) {
                JSONArray(File(importDir, "setlists.json").readText())
            } else {
                null
            }

            return DatabaseTransferData(
                songDtos = songsJson.toJSONObjectList().map { SongDto(it) },
                categoryDtos = categoriesJson.toJSONObjectList().map { CategoryDto(it) },
                setlistDtos = setlistsJson?.toJSONObjectList()?.map { SetlistDto(it) }
            )
        } catch (exception: Exception) {
            Log.e(TAG, exception.stackTraceToString())
            return null
        } finally {
            importDir.deleteRecursively()
        }
    }

    private suspend fun importOpenSong(uri: Uri) {
        val dialogFragment = ProgressDialogFragment(getString(R.string.main_activity_loading_file))
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog
        )
        dialogFragment.show(supportFragmentManager, ProgressDialogFragment.TAG)

        val importedSongs: Set<SongDto>? = withContext(Dispatchers.IO) {
            val importSongXmlParser =
                ImportSongXmlParserFactory.create(filesDir, SongXmlParserType.OPEN_SONG)

            return@withContext try {
                importSongXmlParser.parseZip(contentResolver, uri)
            } catch (exception: Exception) {
                Log.e(TAG, exception.stackTraceToString())
                null
            }
        }

        if (importedSongs == null) {
            dialogFragment.message = getString(R.string.main_activity_import_incorrect_file_format)
            dialogFragment.setErrorColor(true)
            dialogFragment.setShowOkButton(true)
            return
        }

        val colors: IntArray = resources.getIntArray(R.array.category_color_values)
        val importOptions = ImportOptions(
            mImportDialogViewModel.deleteAll,
            mImportDialogViewModel.replaceOnConflict,
            colors
        )

        dataTransferRepository.importSongs(
            importedSongs,
            dialogFragment.messageLiveData,
            importOptions
        )
        dialogFragment.dismiss()
    }

    private fun startImport(): Boolean {
        mImportDialogViewModel.accepted.removeObservers(this)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        mImportChooserResultLauncher.launch(chooserIntent)
        return true
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun goToCategoryManager(): Boolean {
        val intent = Intent(baseContext, CategoryManagerActivity::class.java)
        startActivity(intent)
        return true
    }
}