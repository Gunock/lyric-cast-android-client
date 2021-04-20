/*
 * Created by Tomasz Kiljanczyk on 4/20/21 11:05 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 11:05 AM
 */

package pl.gunock.lyriccast.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat
import pl.gunock.lyriccast.datatransfer.enums.SongXmlParserType
import pl.gunock.lyriccast.datatransfer.extensions.toJSONObjectList
import pl.gunock.lyriccast.datatransfer.factories.ImportSongXmlParserFactory
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SetlistDto
import pl.gunock.lyriccast.datatransfer.models.SongDto
import pl.gunock.lyriccast.fragments.dialogs.ImportDialogFragment
import pl.gunock.lyriccast.fragments.dialogs.ProgressDialogFragment
import pl.gunock.lyriccast.fragments.viewholders.ImportDialogViewModel
import pl.gunock.lyriccast.listeners.ItemSelectedTabListener
import java.io.File

class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"
        const val EXPORT_RESULT_CODE = 1
        const val IMPORT_RESULT_CODE = 2
    }

    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.Factory(resources)
    }

    private lateinit var mImportDialogViewModel: ImportDialogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        // TODO: Possible leak
        mImportDialogViewModel = ViewModelProvider(this).get(ImportDialogViewModel::class.java)

        findViewById<View>(R.id.cstl_fab_container).visibility = View.GONE
        setUpListeners()
    }

    override fun onDestroy() {
        mDatabaseViewModel.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider

        castActionProvider.routeSelector = CastContext.getSharedInstance()!!.mergedSelector

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val uri: Uri = data?.data!!
        when (requestCode) {
            IMPORT_RESULT_CODE -> {
                Log.d(TAG, "Handling import result")
                Log.d(TAG, "Import parameters $mImportDialogViewModel")
                Log.d(TAG, "Selected file URI: $uri")
                if (mImportDialogViewModel.importFormat == ImportFormat.OPEN_SONG) {
                    importOpenSong(uri)
                } else if (mImportDialogViewModel.importFormat == ImportFormat.LYRIC_CAST) {
                    importLyricCast(uri)
                }
            }
            EXPORT_RESULT_CODE -> exportAll(uri)
        }
    }

    private fun setUpListeners() {
        val fabAdd: View = findViewById<FloatingActionButton>(R.id.fab_add)

        val mainTabLayout: TabLayout = findViewById(R.id.tbl_main_fragments)
        mainTabLayout.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                tab ?: return@ItemSelectedTabListener

                fabAdd.clearFocus()

                val navController = findNavController(R.id.navh_main)
                if (tab.text == getString(R.string.title_songs)) {
                    Log.d(TAG, "Switching to song list")
                    navController.navigate(R.id.action_Setlists_to_Songs)
                } else if (tab.text == getString(R.string.title_setlists)) {
                    Log.d(TAG, "Switching to setlists")
                    navController.navigate(R.id.action_Songs_to_Setlists)
                }
            })

        val fabContainer = findViewById<View>(R.id.cstl_fab_container)
        fabAdd.setOnClickListener {
            if (fabContainer.isVisible) {
                fabContainer.visibility = View.GONE
                fabAdd.clearFocus()
            } else {
                fabContainer.visibility = View.VISIBLE
                fabAdd.requestFocus()
            }
        }
        fabAdd.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                fabContainer.visibility = View.VISIBLE
            } else {
                fabContainer.visibility = View.GONE
            }
        }

        findViewById<FloatingActionButton>(R.id.fab_add_setlist).setOnClickListener {
            val intent = Intent(baseContext, SetlistEditorActivity::class.java)
            startActivity(intent)
            fabAdd.clearFocus()
        }

        findViewById<FloatingActionButton>(R.id.fab_add_song).setOnClickListener {
            val intent = Intent(baseContext, SongEditorActivity::class.java)
            startActivity(intent)
            fabAdd.clearFocus()
        }
    }

    private fun startExport(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        startActivityForResult(chooserIntent, EXPORT_RESULT_CODE)

        return true
    }

    private fun exportAll(uri: Uri) {
        val dialogFragment =
            ProgressDialogFragment(getString(R.string.main_activity_export_preparing_data))
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog
        )
        dialogFragment.show(supportFragmentManager, ProgressDialogFragment.TAG)

        val exportData = mDatabaseViewModel.getDatabaseTransferData()

        CoroutineScope(Dispatchers.IO).launch {
            val exportDir = File(filesDir.canonicalPath, ".export")
            exportDir.deleteRecursively()
            exportDir.mkdirs()


            dialogFragment.message = getString(R.string.main_activity_export_saving_json)
            val songsString = JSONArray(exportData.songDtos!!.map { it.toJson() }).toString()
            val categoriesString =
                JSONArray(exportData.categoryDtos!!.map { it.toJson() }).toString()
            val setlistsString = JSONArray(exportData.setlistDtos!!.map { it.toJson() }).toString()

            File(exportDir, "songs.json").writeText(songsString)
            File(exportDir, "categories.json").writeText(categoriesString)
            File(exportDir, "setlists.json").writeText(setlistsString)

            dialogFragment.message = getString(R.string.main_activity_export_saving_zip)
            FileHelper.zip(contentResolver, uri, exportDir.path)

            dialogFragment.message = getString(R.string.main_activity_export_deleting_temp)
            exportDir.deleteRecursively()
            dialogFragment.dismiss()
        }
    }

    private fun showImportDialog(): Boolean {
        val dialogFragment = ImportDialogFragment()
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog_NoTitle
        )

        mImportDialogViewModel.accepted.value = false
        mImportDialogViewModel.accepted.observe(this) { if (it) startImport() }
        dialogFragment.show(supportFragmentManager, ImportDialogFragment.TAG)
        return true
    }

    private fun importLyricCast(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val dialogFragment = ProgressDialogFragment(getString(R.string.main_activity_loading_file))
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog
        )
        dialogFragment.show(supportFragmentManager, ProgressDialogFragment.TAG)
        CoroutineScope(Dispatchers.IO).launch {
            val importDir = File(filesDir.path, ".import")
            importDir.deleteRecursively()
            importDir.mkdirs()

            FileHelper.unzip(contentResolver, uri, importDir.path)
            @Suppress("BlockingMethodInNonBlockingContext")
            inputStream.close()

            val songsJson: JSONArray
            val categoriesJson: JSONArray
            val setlistsJson: JSONArray?
            try {
                songsJson = JSONArray(File(importDir, "songs.json").readText())
                categoriesJson = JSONArray(File(importDir, "categories.json").readText())

                val setlistsFile = File(importDir, "setlists.json")
                setlistsJson = if (setlistsFile.exists()) {
                    JSONArray(File(importDir, "setlists.json").readText())
                } else {
                    null
                }
            } catch (exception: Exception) {
                Log.e(TAG, exception.stackTraceToString())
                dialogFragment.message =
                    getString(R.string.main_activity_import_incorrect_file_format)
                CoroutineScope(Dispatchers.Main).launch {
                    dialogFragment.setErrorColor(true)
                    dialogFragment.setShowOkButton(true)
                }
                return@launch
            }
            importDir.deleteRecursively()

            val transferData = DatabaseTransferData(
                songDtos = songsJson.toJSONObjectList().map { SongDto(it) },
                categoryDtos = categoriesJson.toJSONObjectList().map { CategoryDto(it) },
                setlistDtos = setlistsJson?.toJSONObjectList()?.map { SetlistDto(it) }
            )

            val importOptions = ImportOptions(
                mImportDialogViewModel.deleteAll,
                mImportDialogViewModel.replaceOnConflict
            )

            CoroutineScope(Dispatchers.Main).launch {
                mDatabaseViewModel.importSongs(
                    transferData,
                    dialogFragment.messageLiveData,
                    importOptions
                )

                dialogFragment.dismiss()
            }
        }
    }

    private fun importOpenSong(uri: Uri) {
        val dialogFragment = ProgressDialogFragment(getString(R.string.main_activity_loading_file))
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Dialog
        )
        dialogFragment.show(supportFragmentManager, ProgressDialogFragment.TAG)
        CoroutineScope(Dispatchers.IO).launch {
            val importSongXmlParser =
                ImportSongXmlParserFactory.create(filesDir, SongXmlParserType.OPEN_SONG)

            val importedSongs: Set<SongDto> = try {
                importSongXmlParser.parseZip(contentResolver, uri)
            } catch (exception: Exception) {
                Log.e(TAG, exception.stackTraceToString())
                dialogFragment.message =
                    getString(R.string.main_activity_import_incorrect_file_format)
                CoroutineScope(Dispatchers.Main).launch {
                    dialogFragment.setErrorColor(true)
                    dialogFragment.setShowOkButton(true)
                }
                return@launch
            }

            val colors: IntArray = resources.getIntArray(R.array.category_color_values)
            val importOptions = ImportOptions(
                mImportDialogViewModel.deleteAll,
                mImportDialogViewModel.replaceOnConflict,
                colors
            )
            CoroutineScope(Dispatchers.Main).launch {
                mDatabaseViewModel.importSongs(
                    importedSongs,
                    dialogFragment.messageLiveData,
                    importOptions
                )
                dialogFragment.dismiss()
            }
        }
    }

    private fun startImport(): Boolean {
        mImportDialogViewModel.accepted.removeObservers(this)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        startActivityForResult(chooserIntent, IMPORT_RESULT_CODE)
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