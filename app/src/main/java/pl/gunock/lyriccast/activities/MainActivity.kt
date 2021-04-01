/*
 * Created by Tomasz Kiljanczyk on 4/1/21 11:57 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 11:55 PM
 */

package pl.gunock.lyriccast.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.LyricCastApplication
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.dataimport.ImportSongXmlParserFactory
import pl.gunock.lyriccast.dataimport.enums.SongXmlParserType
import pl.gunock.lyriccast.dataimport.models.ImportSong
import pl.gunock.lyriccast.datamodel.LyricCastRepository
import pl.gunock.lyriccast.datamodel.LyricCastViewModel
import pl.gunock.lyriccast.datamodel.LyricCastViewModelFactory
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.LyricsSection
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections
import pl.gunock.lyriccast.fragments.dialogs.ImportDialogFragment
import pl.gunock.lyriccast.fragments.dialogs.ProgressDialogFragment
import pl.gunock.lyriccast.fragments.viewholders.ImportDialogViewModel
import pl.gunock.lyriccast.helpers.MessageHelper
import pl.gunock.lyriccast.listeners.ItemSelectedTabListener

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        const val EXPORT_RESULT_CODE = 1
        const val IMPORT_RESULT_CODE = 2
    }

    private var castContext: CastContext? = null

    private lateinit var repository: LyricCastRepository
    private val lyricCastViewModel: LyricCastViewModel by viewModels {
        LyricCastViewModelFactory((application as LyricCastApplication).repository)
    }

    private lateinit var importDialogViewModel: ImportDialogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MessageHelper.initialize(applicationContext)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        // TODO: Possible leak
        importDialogViewModel = ViewModelProvider(this).get(ImportDialogViewModel::class.java)

        findViewById<View>(R.id.cstl_fab_container).visibility = View.GONE
        setUpListeners()

        castContext = CastContext.getSharedInstance(this)
        repository = (application as LyricCastApplication).repository
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        CastButtonFactory.setUpMediaRouteButton(
            baseContext,
            menu,
            R.id.menu_cast
        )

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_category_manager -> goToCategoryManager()
            R.id.menu_settings -> goToSettings()
            R.id.menu_import_songs -> showImportDialog()
            R.id.menu_export_all -> export()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val uri = data?.data!!
        when (requestCode) {
            IMPORT_RESULT_CODE -> {
                Log.d(TAG, "Handling import result")
                Log.d(TAG, "Import parameters $importDialogViewModel")
                Log.d(TAG, "Selected file URI: $uri")


                val inputStream = contentResolver.openInputStream(uri) ?: return
                val dialogFragment = ProgressDialogFragment("Loading file ...")
                dialogFragment.setStyle(
                    DialogFragment.STYLE_NORMAL,
                    R.style.Theme_LyricCast_Light_Dialog
                )
                dialogFragment.show(supportFragmentManager, ProgressDialogFragment.TAG)
                CoroutineScope(Dispatchers.IO).launch {
                    val importSongXmlParser =
                        ImportSongXmlParserFactory.create(filesDir, SongXmlParserType.OPEN_SONG)

                    val importedSongs = importSongXmlParser.parseZip(contentResolver, inputStream)
                    @Suppress("BlockingMethodInNonBlockingContext")
                    inputStream.close()

                    loadSongsToDatabase(importedSongs, dialogFragment)
                    dialogFragment.dismiss()
                }
            }
            // TODO: Reimplement export
            EXPORT_RESULT_CODE -> {
//                FileHelper.zip(contentResolver.openOutputStream(uri)!!, filesDir.path)
//                val intent = Intent(baseContext, MainActivity::class.java)
//                startActivity(intent)
//                finish()
            }
        }
    }

    private fun setUpListeners() {
        val mainTabLayout: TabLayout = findViewById(R.id.tbl_main_fragments)
        mainTabLayout.addOnTabSelectedListener(
            ItemSelectedTabListener { tab ->
                tab ?: return@ItemSelectedTabListener

                val navController = findNavController(R.id.navh_main)

                if (tab.text == getString(R.string.label_songs)) {
                    Log.d(TAG, "Switching to song list")
                    navController.navigate(R.id.action_Setlists_to_Songs)
                } else if (tab.text == getString(R.string.label_setlists)) {
                    Log.d(TAG, "Switching to setlists")
                    navController.navigate(R.id.action_Songs_to_Setlists)
                }
            })

        val fabContainer = findViewById<View>(R.id.cstl_fab_container)
        val fabAdd: View = findViewById<FloatingActionButton>(R.id.fab_add)
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
        }

        findViewById<FloatingActionButton>(R.id.fab_add_song).setOnClickListener {
            val intent = Intent(baseContext, SongEditorActivity::class.java)
            startActivity(intent)
        }
    }

    private fun export(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        startActivityForResult(chooserIntent, EXPORT_RESULT_CODE)

        return true
    }

    private fun showImportDialog(): Boolean {
        val dialogFragment = ImportDialogFragment()
        dialogFragment.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.Theme_LyricCast_Light_Dialog
        )

        importDialogViewModel.accepted.value = false
        importDialogViewModel.accepted.observe(this) { if (it) import() }
        dialogFragment.show(supportFragmentManager, ImportDialogFragment.TAG)
        return true
    }

    private fun import(): Boolean {
        importDialogViewModel.accepted.removeObservers(this)

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        startActivityForResult(chooserIntent, IMPORT_RESULT_CODE)
        return true
    }

    private suspend fun loadSongsToDatabase(
        importSongs: Set<ImportSong>,
        dialogFragment: ProgressDialogFragment
    ) {
        dialogFragment.setMessage("Importing categories ...")
        val colors = resources.getIntArray(R.array.category_color_values)

        var processedImportSongs = importSongs.toMutableSet()
        if (!importDialogViewModel.deleteAll && !importDialogViewModel.replaceOnConflict) {
            lyricCastViewModel.getAllSongs().forEach { song ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    processedImportSongs.removeIf { it.title == song.title }
                } else {
                    processedImportSongs = processedImportSongs
                        .filter { it.title != song.title }
                        .toMutableSet()
                }
            }
        }

        val categoryMap: MutableMap<String, Category> =
            processedImportSongs.map { importSong -> importSong.category }
                .distinct()
                .mapIndexed { index, categoryName ->
                    categoryName to Category(
                        name = categoryName.take(30),
                        color = colors[index % colors.size]
                    )
                }.toMap()
                .toMutableMap()
        categoryMap.remove("")

        if (importDialogViewModel.deleteAll) {
            repository.clear()
        }

        val allCategories = lyricCastViewModel.getAllCategories()
        if (!importDialogViewModel.deleteAll && !importDialogViewModel.replaceOnConflict) {
            allCategories.forEach { categoryMap.remove(it.name) }
        }

        lyricCastViewModel.upsertCategories(categoryMap.values)

        dialogFragment.setMessage("Importing songs ...")
        val categoryIdMap = lyricCastViewModel.getAllCategories()
            .map { it.name to it.categoryId }
            .toMap()

        val orderMap: MutableMap<String, List<Pair<String, Int>>> = mutableMapOf()
        val songsWithLyricsSections: List<SongWithLyricsSections> =
            processedImportSongs.map { importSong ->
                val song =
                    Song(title = importSong.title, categoryId = categoryIdMap[importSong.category])
                val lyricsSections: List<LyricsSection> =
                    importSong.lyrics.map {
                        LyricsSection(
                            songId = 0,
                            name = it.key,
                            text = it.value
                        )
                    }
                val order: List<Pair<String, Int>> = importSong.presentation
                    .mapIndexed { index, sectionName -> sectionName to index }

                orderMap[importSong.title] = order
                return@map SongWithLyricsSections(song, lyricsSections)
            }

        lyricCastViewModel.upsertSongs(songsWithLyricsSections, orderMap)

        dialogFragment.setMessage("Finishing import ...")
        Log.d(TAG, "Finished import")
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