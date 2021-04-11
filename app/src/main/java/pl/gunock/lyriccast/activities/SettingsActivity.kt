/*
 * Created by Tomasz Kiljanczyk on 4/11/21 2:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 2:08 AM
 */

package pl.gunock.lyriccast.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.models.LyricCastSettings


class SettingsActivity : AppCompatActivity() {

    private var mPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "appTheme") {
                val settings = LyricCastSettings(baseContext)
                AppCompatDelegate.setDefaultNightMode(settings.appTheme)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar_settings))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fml_settings, SettingsFragment())
                .commit()
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        PreferenceManager.getDefaultSharedPreferences(baseContext)
            .registerOnSharedPreferenceChangeListener(mPreferenceChangeListener)
    }

    override fun onDestroy() {
        mPreferenceChangeListener = null
        super.onDestroy()
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val contextThemeWrapper: Context =
                ContextThemeWrapper(activity, R.style.Theme_LyricCast_Dialog)

            val localInflater = inflater.cloneInContext(contextThemeWrapper)

            return super.onCreateView(localInflater, container, savedInstanceState)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }
    }
}