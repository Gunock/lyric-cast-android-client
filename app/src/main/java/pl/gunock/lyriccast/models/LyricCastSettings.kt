/*
 * Created by Tomasz Kiljanczyk on 4/20/21 11:03 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 10:50 AM
 */

package pl.gunock.lyriccast.models

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONObject
import pl.gunock.lyriccast.R

class LyricCastSettings(context: Context) {
    val blankedOnStart by lazy {
        mPreferences.getBoolean(mCastBlankKey, false)
    }

    val appTheme by lazy { mPreferences.getString(mAppThemeKey, mAppThemeKeyDefault)!!.toInt() }

    private val mPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val mCastBackgroundColorDefault =
        context.getString(R.string.preference_cast_background_color_default_value)
    private val mCastFontColorKeyDefault =
        context.getString(R.string.preference_cast_font_color_default_value)
    private val mAppThemeKeyDefault = context.getString(R.string.preference_theme_default_value)

    private val mCastBlankKey = context.getString(R.string.preference_blank_key)
    private val mCastBackgroundColorKey = context.getString(R.string.preference_cast_background_key)
    private val mCastFontColorKey = context.getString(R.string.preference_cast_font_color_key)
    private val mCastMaxFontSizeKey = context.getString(R.string.preference_cast_max_font_size_key)

    private val mAppThemeKey = context.getString(R.string.preference_theme_key)

    private val mCastBackgroundColor by lazy {
        mPreferences.getString(mCastBackgroundColorKey, mCastBackgroundColorDefault)
    }
    private val mCastFontColor by lazy {
        mPreferences.getString(mCastFontColorKey, mCastFontColorKeyDefault)
    }
    private val mCastMaxFontSize by lazy {
        mPreferences.getInt(mCastMaxFontSizeKey, 100)
    }

    fun getCastConfigurationJson(): JSONObject {
        return JSONObject().apply {
            put("backgroundColor", mCastBackgroundColor)
            put("fontColor", mCastFontColor)
            put("maxFontSize", mCastMaxFontSize)
        }
    }

}