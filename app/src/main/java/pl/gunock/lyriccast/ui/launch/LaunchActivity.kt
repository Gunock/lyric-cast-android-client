/*
 * Created by Tomasz Kiljanczyk on 26/12/2022, 17:04
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 26/12/2022, 17:02
 */

package pl.gunock.lyriccast.ui.launch

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.shared.extensions.registerForActivityResult
import pl.gunock.lyriccast.ui.main.MainActivity

class LaunchActivity : AppCompatActivity() {

    private val turnOnWifiManagerResultLauncher = registerForActivityResult { goToMain() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_launch)
    }

    override fun onStart() {
        super.onStart()

        checkWifiEnabled()
    }

    private fun checkWifiEnabled() {
        val wifiManager = baseContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            turnOnWifi()
        } else {
            goToMain()
        }
    }

    private fun turnOnWifi() {
        var buttonClicked = false
        AlertDialog.Builder(this, R.style.Theme_LyricCast_Dialog_NoTitle)
            .setMessage(getString(R.string.launch_activity_turn_on_wifi))
            .setPositiveButton(getString(R.string.launch_activity_go_to_settings)) { _, _ ->
                buttonClicked = true
                openWifiSettings()
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

    private fun openWifiSettings() {
        val wifiIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
        } else {
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        }
        turnOnWifiManagerResultLauncher.launch(wifiIntent)
    }

}