/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:06
 */

package dev.thomas_kiljanczyk.lyriccast.application

import org.json.JSONObject

fun AppSettings.getCastConfigurationJson(): JSONObject {
    val configuration = JSONObject()
    configuration.put("backgroundColor", this.backgroundColor)
    configuration.put("fontColor", this.fontColor)
    configuration.put("maxFontSize", this.maxFontSize)
    return configuration
}