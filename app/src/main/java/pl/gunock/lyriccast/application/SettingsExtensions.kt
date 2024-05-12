/*
 * Created by Tomasz Kiljanczyk on 12/12/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/12/2021, 00:05
 */

package pl.gunock.lyriccast.application

import org.json.JSONObject

fun AppSettings.getCastConfigurationJson(): JSONObject {
    val configuration = JSONObject()
    configuration.put("backgroundColor", this.backgroundColor)
    configuration.put("fontColor", this.fontColor)
    configuration.put("maxFontSize", this.maxFontSize)
    return configuration
}