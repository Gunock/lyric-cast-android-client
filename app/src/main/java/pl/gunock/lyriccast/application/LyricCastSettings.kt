/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 17/07/2021, 12:31
 */

package pl.gunock.lyriccast.application

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.json.JSONObject
import pl.gunock.lyriccast.R

object LyricCastSettings {
    val blankedOnStart: Boolean get() = mPreferences.getBoolean(mCastBlankKey, false)

    val appTheme: Int get() = mPreferences.getString(mAppThemeKey, mAppThemeKeyDefault)!!.toInt()

    val enableAds: Boolean get() = mPreferences.getBoolean("preference_ads_enable", true)

    private lateinit var mAppThemeKey: String

    private lateinit var mCastBackgroundColorDefault: String
    private lateinit var mCastFontColorKeyDefault: String
    private lateinit var mAppThemeKeyDefault: String

    private lateinit var mCastBlankKey: String
    private lateinit var mCastBackgroundColorKey: String
    private lateinit var mCastFontColorKey: String
    private lateinit var mCastMaxFontSizeKey: String

    private lateinit var mPreferences: SharedPreferences

    private val mCastBackgroundColor: String?
        get() = mPreferences.getString(mCastBackgroundColorKey, mCastBackgroundColorDefault)

    private val mCastFontColor: String?
        get() = mPreferences.getString(mCastFontColorKey, mCastFontColorKeyDefault)

    private val mCastMaxFontSize: Int
        get() = mPreferences.getInt(mCastMaxFontSizeKey, 100)

    fun initialize(context: Context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        mAppThemeKey = context.getString(R.string.preference_theme_key)

        mCastBackgroundColorDefault =
            context.getString(R.string.preference_cast_background_color_default_value)
        mCastFontColorKeyDefault =
            context.getString(R.string.preference_cast_font_color_default_value)
        mAppThemeKeyDefault = context.getString(R.string.preference_theme_default_value)

        mCastBlankKey = context.getString(R.string.preference_blank_key)
        mCastBackgroundColorKey = context.getString(R.string.preference_cast_background_key)
        mCastFontColorKey = context.getString(R.string.preference_cast_font_color_key)
        mCastMaxFontSizeKey = context.getString(R.string.preference_cast_max_font_size_key)
    }

    fun getCastConfigurationJson(): JSONObject {
        return JSONObject().apply {
            put("backgroundColor", mCastBackgroundColor)
            put("fontColor", mCastFontColor)
            put("maxFontSize", mCastMaxFontSize)
        }
    }
}