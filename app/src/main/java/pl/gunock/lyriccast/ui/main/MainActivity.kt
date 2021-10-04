/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 15:48
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
import androidx.activity.viewModels
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivityMainBinding
import pl.gunock.lyriccast.databinding.ContentMainBinding
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.shared.extensions.loadAd
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.shared.utils.DialogFragmentUtils
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import pl.gunock.lyriccast.ui.main.setlists.SetlistsFragment
import pl.gunock.lyriccast.ui.setlist_editor.SetlistEditorActivity
import pl.gunock.lyriccast.ui.settings.SettingsActivity
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedTabListener
import pl.gunock.lyriccast.ui.song_editor.SongEditorActivity
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"
    }

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var dataTransferRepository: DataTransferRepository

    private lateinit var importDialogViewModel: ImportDialogViewModel
    private lateinit var binding: ContentMainBinding

    private val exportChooserResultLauncher = registerForActivityResult(this::exportAll)
    private val importChooserResultLauncher = registerForActivityResult(this::import)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarMain)
        binding = rootBinding.contentMain
        viewModel // Initializes view model

        importDialogViewModel = ViewModelProvider(this).get(ImportDialogViewModel::class.java)

        binding.cstlFabContainer.visibility = View.GONE

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navh_main) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.first()
        if (currentFragment is SetlistsFragment) {
            binding.tblMainFragments.getTabAt(1)?.select()
        }

        setupListeners()
    }

    override fun onResume() {
        binding.advMain.loadAd()

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
            R.id.menu_import_songs -> {
                lifecycleScope.launch(Dispatchers.Main) { showImportDialog() }
                true
            }
            R.id.menu_export_all -> startExport()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        binding.tblMainFragments.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                tab ?: return@ItemSelectedTabListener

                binding.fabAdd.clearFocus()

                val navController = findNavController(R.id.navh_main)
                if (tab.text == getString(R.string.title_songs)) {
                    Log.d(TAG, "Switching to song list")
                    navController.navigate(R.id.action_Setlists_to_Songs)
                } else if (tab.text == getString(R.string.title_setlists)) {
                    Log.d(TAG, "Switching to setlists")
                    navController.navigate(R.id.action_Songs_to_Setlists)
                }
            })

        val fabContainer = binding.cstlFabContainer
        binding.fabAdd.setOnClickListener {
            if (fabContainer.isVisible) {
                fabContainer.visibility = View.GONE
                binding.fabAdd.clearFocus()
            } else {
                fabContainer.visibility = View.VISIBLE
                binding.fabAdd.requestFocus()
            }
        }
        binding.fabAdd.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                fabContainer.visibility = View.VISIBLE
            } else {
                fabContainer.visibility = View.GONE
            }
        }

        binding.fabAddSetlist.setOnClickListener {
            val intent = Intent(baseContext, SetlistEditorActivity::class.java)
            startActivity(intent)
            binding.fabAdd.clearFocus()
        }

        binding.fabAddSong.setOnClickListener {
            val intent = Intent(baseContext, SongEditorActivity::class.java)
            startActivity(intent)
            binding.fabAdd.clearFocus()
        }
    }

    private fun startExport(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        exportChooserResultLauncher.launch(chooserIntent)

        return true
    }

    private fun import(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }

        val uri: Uri = result.data!!.data!!

        Log.d(TAG, "Handling import result")
        Log.d(TAG, "Import parameters $importDialogViewModel")
        Log.d(TAG, "Selected file URI: $uri")
        if (importDialogViewModel.importFormat == ImportFormat.OPEN_SONG) {
            importOpenSong(uri)
        } else if (importDialogViewModel.importFormat == ImportFormat.LYRIC_CAST) {
            importLyricCast(uri)
        }
    }

    private fun exportAll(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }

        lifecycleScope.launch(Dispatchers.Default) {
            val uri: Uri = result.data!!.data!!

            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    supportFragmentManager,
                    R.string.main_activity_export_preparing_data
                )

            @Suppress("BlockingMethodInNonBlockingContext")
            contentResolver.openOutputStream(uri)!!.use { outputStream ->
                viewModel.exportAll(
                    cacheDir.canonicalPath,
                    outputStream,
                    dialogFragment.messageResourceId
                )
            }

            dialogFragment.dismiss()
        }
    }

    private suspend fun showImportDialog() {
        val importDialog = ImportDialogFragment().apply {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_LyricCast_Dialog_NoTitle)
            show(this@MainActivity.supportFragmentManager, ImportDialogFragment.TAG)
        }

        while (!importDialog.isAdded) {
            delay(10)
        }

        importDialog.isAccepted
            .observe(importDialog.viewLifecycleOwner) { if (it) startImport() }
    }

    private fun importLyricCast(uri: Uri) {
        lifecycleScope.launch(Dispatchers.Default) {
            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    supportFragmentManager,
                    R.string.main_activity_loading_file
                )

            val importOptions = ImportOptions(
                importDialogViewModel.deleteAll,
                importDialogViewModel.replaceOnConflict
            )

            @Suppress("BlockingMethodInNonBlockingContext")
            val importSucceeded = contentResolver.openInputStream(uri)!!.use { inputStream ->
                viewModel.importLyricCast(
                    cacheDir.path,
                    inputStream,
                    dialogFragment.messageResourceId,
                    dialogFragment.isError,
                    importOptions
                )
            }

            if (importSucceeded) {
                dialogFragment.dismiss()
            }
        }
    }

    private fun importOpenSong(uri: Uri) {
        lifecycleScope.launch(Dispatchers.Default) {
            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    supportFragmentManager,
                    R.string.main_activity_loading_file
                )

            val colors: IntArray = resources.getIntArray(R.array.category_color_values)
            val importOptions = ImportOptions(
                importDialogViewModel.deleteAll,
                importDialogViewModel.replaceOnConflict,
                colors
            )

            @Suppress("BlockingMethodInNonBlockingContext")
            val importSucceeded = contentResolver.openInputStream(uri)!!.use { inputStream ->
                viewModel.importOpenSong(
                    cacheDir.path,
                    inputStream,
                    dialogFragment.messageResourceId,
                    dialogFragment.isError,
                    importOptions
                )
            }

            if (importSucceeded) {
                dialogFragment.dismiss()
            }
        }
    }

    private fun startImport(): Boolean {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        importChooserResultLauncher.launch(chooserIntent)
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