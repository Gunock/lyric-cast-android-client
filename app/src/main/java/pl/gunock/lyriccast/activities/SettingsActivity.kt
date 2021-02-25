/*
 * Created by Tomasz Kiljańczyk on 2/25/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/25/21 9:59 PM
 */

package pl.gunock.lyriccast.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.cast.framework.CastContext
import pl.gunock.lyriccast.R

class SettingsActivity : AppCompatActivity() {
    private companion object {
        private const val TAG = "SettingsActivity"
    }

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