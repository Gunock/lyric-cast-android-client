/*
 * Created by Tomasz Kiljanczyk on 4/11/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/11/21 9:23 PM
 */

package pl.gunock.lyriccast.models

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONObject
import pl.gunock.lyriccast.R

class LyricCastSettings(context: Context) {
    private val mPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val mCastBackgroundColorDefault =
        context.getString(R.string.preference_cast_background_color_default_value)
    private val mCastFontColorKeyDefault =
        context.getString(R.string.preference_cast_font_color_default_value)
    private val mAppThemeKeyDefault = context.getString(R.string.preference_theme_default_value)

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
    val appTheme by lazy { mPreferences.getString(mAppThemeKey, mAppThemeKeyDefault)!!.toInt() }

    fun getCastConfigurationJson(): JSONObject {
        return JSONObject().apply {
            put("backgroundColor", mCastBackgroundColor)
            put("fontColor", mCastFontColor)
            put("maxFontSize", mCastMaxFontSize)
        }
    }

}