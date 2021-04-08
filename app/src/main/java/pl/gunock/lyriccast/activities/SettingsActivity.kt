/*
 * Created by Tomasz Kiljanczyk on 4/8/21 1:47 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/8/21 1:41 PM
 */

package pl.gunock.lyriccast.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.preference.PreferenceFragmentCompat
import pl.gunock.lyriccast.R


class SettingsActivity : AppCompatActivity() {
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
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            // create ContextThemeWrapper from the original Activity Context with the custom theme
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