/*
 * Created by Tomasz Kiljanczyk on 4/9/21 5:36 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/9/21 5:36 PM
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
    private val mAppThemeKey = context.getString(R.string.preference_theme_key)

    val castBackgroundColor by lazy {
        mPreferences.getString(mCastBackgroundColorKey, mCastBackgroundColorDefault)
    }
    val castFontColor by lazy {
        mPreferences.getString(
            mCastFontColorKey,
            mCastFontColorKeyDefault
        )
    }
    val appTheme by lazy { mPreferences.getString(mAppThemeKey, mAppThemeKeyDefault)!!.toInt() }


    fun getCastConfigurationJson(): JSONObject {
        return JSONObject().apply {
            put("backgroundColor", castBackgroundColor)
            put("fontColor", castFontColor)
        }
    }

}