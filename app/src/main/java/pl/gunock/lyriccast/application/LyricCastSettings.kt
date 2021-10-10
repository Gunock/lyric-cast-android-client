/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 10:03
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 09:44
 */

package pl.gunock.lyriccast.application

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONObject
import pl.gunock.lyriccast.R

object LyricCastSettings {
    var appTheme: Int = 0
        private set

    var controlsButtonHeight: Float = 88f
        private set

    var blankedOnStart: Boolean = false
        private set

    var enableAds: Boolean = true
        private set

    private var castBackgroundColor: String? = null
    private var castFontColor: String? = null
    private var castMaxFontSize: Int = 100

    fun initialize(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val appThemeKey = context.getString(R.string.preference_theme_key)
        val appThemeKeyDefault = context.getString(R.string.preference_theme_default_value)
        appTheme = preferences.getString(appThemeKey, appThemeKeyDefault)!!.toInt()

        val controlsButtonHeightKey =
            context.getString(R.string.preference_controls_button_height_key)
        controlsButtonHeight = preferences.getString(controlsButtonHeightKey, "88")!!.toFloat()

        val blankKey = context.getString(R.string.preference_blank_key)
        blankedOnStart = preferences.getBoolean(blankKey, false)

        val enableAdsKey = context.getString(R.string.preference_enable_ads_key)
        enableAds = preferences.getBoolean(enableAdsKey, true)

        val castBackgroundColorKey = context.getString(R.string.preference_cast_background_key)
        val castBackgroundColorDefault =
            context.getString(R.string.preference_cast_background_color_default_value)
        castBackgroundColor =
            preferences.getString(castBackgroundColorKey, castBackgroundColorDefault)

        val castFontColorKeyDefault =
            context.getString(R.string.preference_cast_font_color_default_value)

        val castFontColorKey = context.getString(R.string.preference_cast_font_color_key)
        castFontColor = preferences.getString(castFontColorKey, castFontColorKeyDefault)

        val castMaxFontSizeKey = context.getString(R.string.preference_cast_max_font_size_key)
        castMaxFontSize = preferences.getInt(castMaxFontSizeKey, 100)
    }

    fun getCastConfigurationJson(): JSONObject {
        return JSONObject().apply {
            put("backgroundColor", castBackgroundColor)
            put("fontColor", castFontColor)
            put("maxFontSize", castMaxFontSize)
        }
    }
}