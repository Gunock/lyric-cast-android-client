/*
 * Created by Tomasz Kiljańczyk on 10/25/20 10:05 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/25/20 9:58 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.cast.framework.CastContext
import pl.gunock.lyriccast.R

class SettingsActivity : AppCompatActivity() {
    private val tag = "SettingsActivity"

    private var castContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar_settings))

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        castContext = CastContext.getSharedInstance(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }
    }
}