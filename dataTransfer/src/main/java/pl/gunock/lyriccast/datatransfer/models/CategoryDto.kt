/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:45 AM
 */

package pl.gunock.lyriccast.datatransfer.models

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
