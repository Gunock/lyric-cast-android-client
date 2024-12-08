/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 19:57
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 22/09/2022, 20:45
 */

package pl.gunock.lyriccast.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.application.settingsDataStore
import pl.gunock.lyriccast.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            var preferenceValue: String
            try {
                preferenceValue = sharedPreferences.getString(key, "")!!
            } catch (e: ClassCastException) {
                val exceptionMessage = e.toString()

                preferenceValue = when {
                    exceptionMessage.contains("java.lang.Integer") -> {
                        sharedPreferences.getInt(key, 0).toString()
                    }

                    exceptionMessage.contains("java.lang.Float") -> {
                        sharedPreferences.getFloat(key, 0.0f).toString()
                    }

                    exceptionMessage.contains("java.lang.Boolean") -> {
                        sharedPreferences.getBoolean(key, false).toString()
                    }

                    else -> throw e
                }
            }

            if (preferenceValue.isBlank()) {
                return@OnSharedPreferenceChangeListener
            }

            runBlocking {
                settingsDataStore.updateData { settings ->
                    val settingsBuilder = settings.toBuilder()
                    when (key) {
                        "appTheme" -> {
                            val appThemeValue = preferenceValue.toInt()
                            settingsBuilder.appTheme = appThemeValue
                        }

                        "controlsButtonHeight" -> {
                            settingsBuilder.controlButtonsHeight = preferenceValue.toFloat()
                        }

                        "blankedOnStart" -> {
                            settingsBuilder.blankOnStart = preferenceValue.toBooleanStrict()
                        }

                        "backgroundColor" -> {
                            settingsBuilder.backgroundColor = preferenceValue
                        }

                        "fontColor" -> {
                            settingsBuilder.fontColor = preferenceValue
                        }

                        "fontMaxSize" -> {
                            settingsBuilder.maxFontSize = preferenceValue.toInt()
                        }
                    }
                    settingsBuilder.build()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSettings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fml_settings, SettingsFragment())
            transaction.commit()
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        PreferenceManager.getDefaultSharedPreferences(baseContext)
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onDestroy() {
        preferenceChangeListener = null
        super.onDestroy()
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val contextThemeWrapper: Context =
                ContextThemeWrapper(activity, R.style.ThemeOverlay_LyricCast_MaterialAlertDialog)

            val localInflater = inflater.cloneInContext(contextThemeWrapper)

            return super.onCreateView(localInflater, container, savedInstanceState)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }
    }
}