/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.models

import org.json.JSONObject

data class CategoryDto(val name: String, val color: Int?) {
    constructor(json: JSONObject) : this(json.getString("name"), json.optInt("color"))

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("color", color ?: JSONObject.NULL)
        }
    }
}
