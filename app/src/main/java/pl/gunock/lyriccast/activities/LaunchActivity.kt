/*
 * Created by Tomasz Kiljańczyk on 3/16/21 4:50 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/16/21 4:50 PM
 */

package pl.gunock.lyriccast.activities

import android.app.Activity
import android.app.AlertDialog
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

    companion object {
        const val TURN_ON_WIFI_RESULT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MessageHelper.initialize(applicationContext)

        setContentView(R.layout.activity_launch)
        setSupportActionBar(findViewById(R.id.toolbar_launch))

        SongsContext.songsDirectory = "${filesDir.path}/songs/"
        CategoriesContext.categoriesDirectory = "${filesDir.path}/categories/"
        SetlistsContext.setlistsDirectory = "${filesDir.path}/setlists/"
    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {
            loadData()

            val wifiManager = baseContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) {
                CoroutineScope(Dispatchers.Main).launch {
                    turnOnWifi()
                }
            } else {
                goToMain()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            TURN_ON_WIFI_RESULT_CODE -> {
                goToMain()
            }
        }
    }

    private fun turnOnWifi() {
        val builder = AlertDialog.Builder(this)
        var buttonClicked = false
        builder.setMessage("Turn on WiFi to enable casting.")
            .setPositiveButton("Go to settings") { _, _ ->
                buttonClicked = true
                val turnWifiOn = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivityForResult(turnWifiOn, TURN_ON_WIFI_RESULT_CODE)
            }
            .setNegativeButton("Ignore") { _, _ ->
                buttonClicked = true
                goToMain()
            }
            .setOnDismissListener {
                if (!buttonClicked) {
                    goToMain()
                }
            }.create().show()
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