/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/14/20 11:44 PM
 */

package pl.gunock.lyriccast

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import pl.gunock.lyriccast.listeners.TabItemSelectedListener
import pl.gunock.lyriccast.utils.FileHelper
import pl.gunock.lyriccast.utils.ResourceHelper
import java.io.File

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"

    private val selectFolderResultCode = 1
    private var castContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ResourceHelper.initialize(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            val turnWifiOn = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(turnWifiOn)
        }

        setupListeners()

        SongsContext.songsDirectory = "${filesDir.path}/songs/"
        castContext = CastContext.getSharedInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        CastButtonFactory.setUpMediaRouteButton(
            applicationContext,
            menu,
            R.id.menu_cast
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_import_songs -> importFiles()
            R.id.menu_settings -> goToSettings()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == selectFolderResultCode && resultCode == Activity.RESULT_OK) {
            val uri = data?.data!!

            Log.d(tag, "Selected file URI: $uri")
            Log.d(tag, "Target path: ${SongsContext.songsDirectory}")

            File(SongsContext.songsDirectory).deleteRecursively()

            FileHelper.unzip(
                contentResolver,
                contentResolver.openInputStream(uri)!!,
                SongsContext.songsDirectory
            )

            SongsContext.loadSongsMetadata()
        }
    }

    private fun setupListeners() {
        findViewById<TabLayout>(R.id.tab_layout_songs_setlists).addOnTabSelectedListener(
            TabItemSelectedListener {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController

                if (it!!.text == getString(R.string.song_list_fragment_label)) {
                    Log.d(tag, "Switching to song list")
                    navController.navigate(R.id.action_SetlistsFragment_to_SongListFragment)
                } else if (it.text == getString(R.string.setlists_fragment_label)) {
                    Log.d(tag, "Switching to setlists")
                    navController.navigate(R.id.action_SongListFragment_to_SetlistsFragment)
                }
            })

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            if (findViewById<LinearLayout>(R.id.fab_view_add_song).isVisible) {
                findViewById<LinearLayout>(R.id.fab_view_add_song).visibility = View.INVISIBLE
                findViewById<LinearLayout>(R.id.fab_view_add_setlist).visibility = View.INVISIBLE
            } else {
                findViewById<LinearLayout>(R.id.fab_view_add_song).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.fab_view_add_setlist).visibility = View.VISIBLE
            }
        }
    }

    private fun importFiles(): Boolean {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/zip"

        val chooserIntent = Intent.createChooser(intent, "Choose a file")
        startActivityForResult(chooserIntent, selectFolderResultCode)
        return true
    }

    private fun goToSettings(): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

}