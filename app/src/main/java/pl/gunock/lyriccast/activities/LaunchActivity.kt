/*
 * Created by Tomasz Kiljańczyk on 3/13/21 4:05 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/13/21 3:50 PM
 */

package pl.gunock.lyriccast.activities

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.CategoriesContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.SetlistsContext
import pl.gunock.lyriccast.SongsContext
import pl.gunock.lyriccast.helpers.MessageHelper

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MessageHelper.initialize(applicationContext)

        setContentView(R.layout.activity_launch)
        setSupportActionBar(findViewById(R.id.toolbar_launch))

        val wifiManager = baseContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            val turnWifiOn = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(turnWifiOn)
        }

        SongsContext.songsDirectory = "${filesDir.path}/songs/"
        CategoriesContext.categoriesDirectory = "${filesDir.path}/categories/"
        SetlistsContext.setlistsDirectory = "${filesDir.path}/setlists/"


    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {
            loadData()
            goToMain()
        }
    }

    private fun loadData() {
        val areSongsNotEmpty = SongsContext.getSongMap().isNotEmpty()
        val areSetlistsNotEmpty = SetlistsContext.getSetlistItems().isNotEmpty()
        val areCategoriesNotEmpty = CategoriesContext.getCategoryItems().isNotEmpty()
        if (areSongsNotEmpty || areSetlistsNotEmpty || areCategoriesNotEmpty) {
            return
        }

        if (!areCategoriesNotEmpty) {
            CategoriesContext.loadCategories()
        }

        if (!areSongsNotEmpty) {
            SongsContext.loadSongsMetadata()
        }

        if (!areCategoriesNotEmpty) {
            SetlistsContext.loadSetlists()
        }
    }

    private fun goToMain() {
        val intent = Intent(baseContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}