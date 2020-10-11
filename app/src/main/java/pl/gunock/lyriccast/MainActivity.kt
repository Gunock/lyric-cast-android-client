/*
 * Created by Tomasz Kiljańczyk on 10/11/20 11:21 PM
 * Copyright (c) 2020 . All rights reserved.
 *  Last modified 10/11/20 8:12 PM
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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
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

        SongsContext.songsDirectory = "${filesDir.path}/songs/"
        castContext = CastContext.getSharedInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        CastButtonFactory.setUpMediaRouteButton(
            applicationContext,
            menu,
            R.id.menu_cast
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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