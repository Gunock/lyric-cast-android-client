/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 20:29
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 12/11/2022, 20:13
 */

package pl.gunock.lyriccast.ui.main

import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.ActivityMainBinding
import pl.gunock.lyriccast.databinding.ContentMainBinding
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat
import pl.gunock.lyriccast.shared.cast.CustomMediaRouteActionProvider
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.shared.utils.DialogFragmentUtils
import pl.gunock.lyriccast.ui.category_manager.CategoryManagerActivity
import pl.gunock.lyriccast.ui.main.import_dialog.ImportDialogFragment
import pl.gunock.lyriccast.ui.main.import_dialog.ImportDialogModel
import pl.gunock.lyriccast.ui.main.setlists.SetlistsFragment
import pl.gunock.lyriccast.ui.setlist_editor.SetlistEditorActivity
import pl.gunock.lyriccast.ui.settings.SettingsActivity
import pl.gunock.lyriccast.ui.shared.fragments.ProgressDialogFragment
import pl.gunock.lyriccast.ui.shared.listeners.ItemSelectedTabListener
import pl.gunock.lyriccast.ui.song_editor.SongEditorActivity
import java.io.Closeable
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"

        var wifiStateChecked = false
    }

    private val viewModel: MainModel by viewModels()
    private val importDialogModel: ImportDialogModel by viewModels()

    private lateinit var binding: ContentMainBinding

    private val exportChooserResultLauncher = registerForActivityResult(this::exportAll)
    private val importChooserResultLauncher = registerForActivityResult(this::import)

    private val castExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val rootBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootBinding.root)
        setSupportActionBar(rootBinding.toolbarMain)

        binding = rootBinding.contentMain

        binding.cstlFabContainer.visibility = View.GONE

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navh_main) as NavHostFragment
        val currentFragment = navHostFragment.childFragmentManager.fragments.first()
        if (currentFragment is SetlistsFragment) {
            binding.tblMainFragments.getTabAt(1)?.select()
        }

        setupListeners()

        if (!wifiStateChecked) {
            checkWifiEnabled()
            wifiStateChecked = true
        }

        setOnApplyWindowInsetsListener(rootBinding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            rootBinding.toolbarMain.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = 0
            }

            binding.tblMainFragments.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                insets.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val castActionProvider =
            MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_cast)) as CustomMediaRouteActionProvider

        // TODO: Apply this approach to every .getSharedInstance() usage
        castActionProvider.routeSelector =
            CastContext.getSharedInstance(this, castExecutor).result.mergedSelector!!

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_category -> {
                goToCategoryManager()
                true
            }

            R.id.menu_settings -> {
                goToSettings()
                true
            }

            R.id.menu_import_songs -> {
                lifecycleScope.launch(Dispatchers.Default) { showImportDialog() }
                true
            }

            R.id.menu_export_all -> {
                startExport()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        binding.tblMainFragments.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                tab ?: return@ItemSelectedTabListener

                val navController = findNavController(R.id.navh_main)
                if (tab.text == getString(R.string.title_songs)) {
                    Log.v(TAG, "Switching to song list")
                    navController.navigate(R.id.action_Setlists_to_Songs)
                } else if (tab.text == getString(R.string.title_setlists)) {
                    Log.v(TAG, "Switching to setlists")
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
                binding.imvMainDim.visibility = View.VISIBLE
            } else {
                fabContainer.visibility = View.GONE
                binding.imvMainDim.visibility = View.GONE
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

    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        exportChooserResultLauncher.launch(chooserIntent)
    }

    private fun import(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) {
            return
        }

        val uri: Uri = result.data!!.data!!

        Log.d(TAG, "Handling import result")
        Log.d(TAG, "Import parameters $importDialogModel")
        Log.d(TAG, "Selected file URI: $uri")
        if (importDialogModel.importFormat == ImportFormat.OPEN_SONG) {
            importOpenSong(uri)
        } else if (importDialogModel.importFormat == ImportFormat.LYRIC_CAST) {
            importLyricCast(uri)
        }
    }

    private fun exportAll(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) {
            return
        }

        lifecycleScope.launch(Dispatchers.Default) {
            val uri: Uri = result.data!!.data!!

            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    supportFragmentManager,
                    R.string.main_activity_export_preparing_data
                )

            val outputStream = contentResolver.openOutputStream(uri)!!

            val exportMessageFlow = viewModel.exportAll(
                cacheDir.canonicalPath,
                outputStream
            )

            withContext(Dispatchers.Main) {
                handleDialogMessages(dialogFragment, exportMessageFlow, outputStream)
            }
        }
    }

    private suspend fun showImportDialog() {
        val importDialog = ImportDialogFragment()
        importDialog.show(this@MainActivity.supportFragmentManager, ImportDialogFragment.TAG)

        while (!importDialog.isAdded) {
            delay(10)
        }

        withContext(Dispatchers.Main) {
            importDialog.isAccepted
                .onEach { if (it) startImport() }
                .flowOn(Dispatchers.Default)
                .launchIn(importDialog.lifecycleScope)
        }
    }

    private fun importLyricCast(uri: Uri) =
        lifecycleScope.launch(Dispatchers.Default) {
            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    supportFragmentManager,
                    R.string.main_activity_loading_file
                )

            val importOptions = ImportOptions(
                deleteAll = importDialogModel.deleteAll,
                replaceOnConflict = importDialogModel.replaceOnConflict
            )

            val inputStream = contentResolver.openInputStream(uri)!!

            val importMessageFlow =
                viewModel.importLyricCast(
                    cacheDir.path,
                    inputStream,
                    importOptions
                )

            withContext(Dispatchers.Main) {
                handleDialogMessages(dialogFragment, importMessageFlow, inputStream)
            }
        }

    private fun importOpenSong(uri: Uri) =
        lifecycleScope.launch(Dispatchers.Default) {
            val dialogFragment =
                DialogFragmentUtils.createProgressDialogFragment(
                    supportFragmentManager,
                    R.string.main_activity_loading_file
                )

            val colors: IntArray = resources.getIntArray(R.array.category_color_values)
            val importOptions = ImportOptions(
                deleteAll = importDialogModel.deleteAll,
                replaceOnConflict = importDialogModel.replaceOnConflict,
                colors = colors
            )

            val inputStream = contentResolver.openInputStream(uri)!!

            val importMessageFlow =
                viewModel.importOpenSong(
                    cacheDir.path,
                    inputStream,
                    importOptions
                )

            withContext(Dispatchers.Main) {
                handleDialogMessages(dialogFragment, importMessageFlow, inputStream)
            }
        }

    private fun handleDialogMessages(
        dialogFragment: ProgressDialogFragment,
        messageFlow: Flow<Int>?,
        stream: Closeable
    ) {
        if (messageFlow != null) {
            messageFlow.onEach { dialogFragment.setMessage(it) }
                .onCompletion {
                    withContext(Dispatchers.IO) {
                        stream.close()
                    }
                    dialogFragment.dismiss()
                }.flowOn(Dispatchers.Main)
                .launchIn(dialogFragment.lifecycleScope)
        } else {
            stream.close()
            dialogFragment.setErrorState(true)
            dialogFragment.setMessage(R.string.main_activity_import_incorrect_file_format)
        }
    }


    private fun startImport() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        importChooserResultLauncher.launch(chooserIntent)
    }

    private fun goToSettings() {
        val intent = Intent(baseContext, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun goToCategoryManager() {
        val intent = Intent(baseContext, CategoryManagerActivity::class.java)
        startActivity(intent)
    }

    private fun checkWifiEnabled() {
        val wifiManager = baseContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            turnOnWifi()
        }
    }

    private fun turnOnWifi() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_LyricCast_MaterialAlertDialog_NoTitle)
            .setMessage(getString(R.string.launch_activity_turn_on_wifi))
            .setPositiveButton(getString(R.string.launch_activity_go_to_settings)) { _, _ ->
                openWifiSettings()
            }
            .setNegativeButton(getString(R.string.launch_activity_ignore), null)
            .create()
            .show()
    }

    private fun openWifiSettings() {
        val wifiIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        }

        startActivity(wifiIntent)
    }
}