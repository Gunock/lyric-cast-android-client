/*
 * Created by Tomasz Kiljanczyk on 4/11/21 2:05 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/10/21 11:59 PM
 */

package pl.gunock.lyriccast.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.cast.framework.CastContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.helpers.MessageHelper
import pl.gunock.lyriccast.models.LyricCastSettings

class LaunchActivity : AppCompatActivity() {

    companion object {
        const val TURN_ON_WIFI_RESULT_CODE = 1
        const val PERMISSIONS_REQUEST_CODE = 1
        val PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MessageHelper.initialize(resources)

        setContentView(R.layout.activity_launch)
        setSupportActionBar(findViewById(R.id.toolbar_launch))

        // Initializes CastContext
        CastContext.getSharedInstance(applicationContext)

        val settings = LyricCastSettings(applicationContext)
        AppCompatDelegate.setDefaultNightMode(settings.appTheme)
    }

    override fun onStart() {
        super.onStart()

        requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != PERMISSIONS_REQUEST_CODE) {
            return
        }

        if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            return
        }

        val wifiManager = baseContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            turnOnWifi()
        } else {
            goToMain()
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
        var buttonClicked = false
        // TODO: Possible leak
        AlertDialog.Builder(this, R.style.Theme_LyricCast_Dialog_NoTitle)
            .setMessage(getString(R.string.launch_activity_turn_on_wifi))
            .setPositiveButton(getString(R.string.launch_activity_go_to_settings)) { _, _ ->
                buttonClicked = true
                val turnWifiOn = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivityForResult(turnWifiOn, TURN_ON_WIFI_RESULT_CODE)
            }
            .setNegativeButton(getString(R.string.launch_activity_ignore)) { _, _ ->
                buttonClicked = true
                goToMain()
            }
            .setOnDismissListener {
                if (!buttonClicked) {
                    goToMain()
                }
            }
            .create()
            .show()
    }

    private fun goToMain() {
        val intent = Intent(baseContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}