/*
 * Created by Tomasz Kiljańczyk on 3/9/21 2:21 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/9/21 2:10 AM
 */

package pl.gunock.lyriccast.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.CategoriesContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.helpers.FileHelper
import pl.gunock.lyriccast.helpers.MessageHelper
import pl.gunock.lyriccast.listeners.ItemSelectedTabListener
import java.io.File

class MainActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "MainActivity"
        const val EXPORT_RESULT_CODE = 1
        const val IMPORT_RESULT_CODE = 2
    }

    private var castContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MessageHelper.initialize(applicationContext)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        findViewById<LinearLayout>(R.id.lns_fab_add_song).visibility = View.GONE
        findViewById<LinearLayout>(R.id.lns_fab_add_setlist).visibility = View.GONE

        val wifiManager = baseContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            val turnWifiOn = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(turnWifiOn)
        }

        setUpListeners()

        SongsContext.songsDirectory = "${filesDir.path}/songs/"
        CategoriesContext.categoriesDirectory = "${filesDir.path}/categories/"
        SetlistsContext.setlistsDirectory = "${filesDir.path}/setlists/"

        castContext = CastContext.getSharedInstance(this)

        CoroutineScope(Dispatchers.Main).launch {
            loadData()
            goToSongs()
        }
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
            R.id.menu_add -> goToCategoryManager()
            R.id.menu_settings -> goToSettings()
            R.id.menu_import_songs -> import()
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
                Log.d(TAG, "Selected file URI: $uri")
                Log.d(TAG, "Target path: ${SongsContext.songsDirectory}")

                File(SongsContext.songsDirectory).deleteRecursively()

                FileHelper.unzip(
                    contentResolver,
                    contentResolver.openInputStream(uri)!!,
                    filesDir.path
                )

                SongsContext.loadSongsMetadata()
                finish()
                val intent = Intent(baseContext, this.javaClass)
                startActivity(intent)
            }
            EXPORT_RESULT_CODE -> {
                FileHelper.zip(contentResolver.openOutputStream(uri)!!, filesDir.path)
                finish()
                val intent = Intent(baseContext, this.javaClass)
                startActivity(intent)
            }
        }
    }

    private fun setUpListeners() {
        findViewById<TabLayout>(R.id.tbl_song_section).addOnTabSelectedListener(
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

        val addSongFab = findViewById<LinearLayout>(R.id.lns_fab_add_song)
        val addSetlistFab = findViewById<LinearLayout>(R.id.lns_fab_add_setlist)
        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            if (addSongFab.isVisible) {
                addSongFab.visibility = View.GONE
                addSetlistFab.visibility = View.GONE
            } else {
                addSongFab.visibility = View.VISIBLE
                addSetlistFab.visibility = View.VISIBLE
            }
        }

        findViewById<FloatingActionButton>(R.id.fab_add_setlist).setOnClickListener {
            val intent = Intent(baseContext, SetlistEditorActivity::class.java)
            startActivity(intent)
            addSongFab.visibility = View.GONE
            addSetlistFab.visibility = View.GONE
        }

        findViewById<FloatingActionButton>(R.id.fab_add_song).setOnClickListener {
            val intent = Intent(baseContext, SongEditorActivity::class.java)
            startActivity(intent)
            addSongFab.visibility = View.GONE
            addSetlistFab.visibility = View.GONE
        }
    }

    private fun loadData() {
        val areSongsNotEmpty = SongsContext.getSongMap().isNotEmpty()
        val areSetlistsNotEmpty = SetlistsContext.getSetlistItems().isNotEmpty()
        val areCategoriesNotEmpty = CategoriesContext.getCategoryItems().isNotEmpty()
        if (areSongsNotEmpty || areSetlistsNotEmpty || areCategoriesNotEmpty) {
            return
        }

        // TODO: Potential leak (verify)
        val alertDialog: AlertDialog = AlertDialog.Builder(this)
            .setMessage("Loading...")
            .create()
        alertDialog.show()

        if (!areCategoriesNotEmpty) {
            CategoriesContext.loadCategories()
        }

        if (!areSongsNotEmpty) {
            SongsContext.loadSongsMetadata()
        }

        if (!areCategoriesNotEmpty) {
            SetlistsContext.loadSetlists()
        }

        alertDialog.hide()
    }

    private fun export(): Boolean {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a directory")
        startActivityForResult(chooserIntent, EXPORT_RESULT_CODE)

        return true
    }

    private fun import(): Boolean {
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

    private suspend fun goToSongs() {
        while (findViewById<FragmentContainerView>(R.id.navh_main) == null) {
            delay(100)
        }

        val navController = findNavController(R.id.navh_main)
        navController.navigate(R.id.action_Start_to_Songs)
    }
}