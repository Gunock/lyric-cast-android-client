/*
 * Created by Tomasz Kiljanczyk on 12/12/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 11/12/2021, 23:12
 */

package pl.gunock.lyriccast.ui.launch

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.shared.extensions.getSettings
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.ui.main.MainActivity

class LaunchActivity : AppCompatActivity() {

    companion object {
        const val PERMISSIONS_REQUEST_CODE = 1
        val PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
    }

    private val turnOnWifiManagerResultLauncher =
        registerForActivityResult(this::handleTurnOnWiFiResult)

    // TODO: Resolve this problem
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_launch)

        AppCompatDelegate.setDefaultNightMode(applicationContext.getSettings().appTheme)
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

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

    private fun handleTurnOnWiFiResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }

        goToMain()
    }

    private fun turnOnWifi() {
        var buttonClicked = false
        AlertDialog.Builder(this, R.style.Theme_LyricCast_Dialog_NoTitle)
            .setMessage(getString(R.string.launch_activity_turn_on_wifi))
            .setPositiveButton(getString(R.string.launch_activity_go_to_settings)) { _, _ ->
                buttonClicked = true
                val turnWifiOn = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                turnOnWifiManagerResultLauncher.launch(turnWifiOn)
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