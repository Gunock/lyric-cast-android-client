/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 10:03
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 09:44
 */

package pl.gunock.lyriccast.application

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.json.JSONObject
import pl.gunock.lyriccast.R

object LyricCastSettings {
    val blankedOnStart: Boolean get() = preferences.getBoolean(castBlankKey, false)

    val appTheme: Int get() = preferences.getString(appThemeKey, appThemeKeyDefault)!!.toInt()

    val enableAds: Boolean get() = preferences.getBoolean("preference_ads_enable", true)

    private lateinit var appThemeKey: String

    private lateinit var castBackgroundColorDefault: String
    private lateinit var castFontColorKeyDefault: String
    private lateinit var appThemeKeyDefault: String

    private lateinit var castBlankKey: String
    private lateinit var castBackgroundColorKey: String
    private lateinit var castFontColorKey: String
    private lateinit var castMaxFontSizeKey: String

    private lateinit var preferences: SharedPreferences

    private val castBackgroundColor: String?
        get() = preferences.getString(castBackgroundColorKey, castBackgroundColorDefault)

    private val castFontColor: String?
        get() = preferences.getString(castFontColorKey, castFontColorKeyDefault)

    private val castMaxFontSize: Int
        get() = preferences.getInt(castMaxFontSizeKey, 100)

    fun initialize(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        appThemeKey = context.getString(R.string.preference_theme_key)

        castBackgroundColorDefault =
            context.getString(R.string.preference_cast_background_color_default_value)
        castFontColorKeyDefault =
            context.getString(R.string.preference_cast_font_color_default_value)
        appThemeKeyDefault = context.getString(R.string.preference_theme_default_value)

        castBlankKey = context.getString(R.string.preference_blank_key)
        castBackgroundColorKey = context.getString(R.string.preference_cast_background_key)
        castFontColorKey = context.getString(R.string.preference_cast_font_color_key)
        castMaxFontSizeKey = context.getString(R.string.preference_cast_max_font_size_key)
    }

    fun getCastConfigurationJson(): JSONObject {
        return JSONObject().apply {
            put("backgroundColor", castBackgroundColor)
            put("fontColor", castFontColor)
            put("maxFontSize", castMaxFontSize)
        }
    }
}