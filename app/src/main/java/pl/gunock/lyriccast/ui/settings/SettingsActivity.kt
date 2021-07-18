/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 21:06
 */

package pl.gunock.lyriccast.ui.settings

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
import pl.gunock.lyriccast.application.LyricCastSettings
import pl.gunock.lyriccast.databinding.ActivitySettingsBinding
import pl.gunock.lyriccast.shared.extensions.loadAd


class SettingsActivity : AppCompatActivity() {

    private var mPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "appTheme") {
                AppCompatDelegate.setDefaultNightMode(LyricCastSettings.appTheme)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSettings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.contentSettings.advSettings1.loadAd()
        binding.contentSettings.advSettings2.loadAd()

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