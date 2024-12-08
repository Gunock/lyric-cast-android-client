/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.extensions

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.toJSONObjectList(): List<JSONObject> {
    val result = mutableListOf<JSONObject>()

    for (i in 0 until this.length()) {
        result.add(this.getJSONObject(i))
    }

    return result
}

fun JSONObject.getStringList(key: String): List<String> {
    val result: MutableList<String> = mutableListOf()

    val jsonArray = this.getJSONArray(key)
    for (i in 0 until jsonArray.length()) {
        result.add(jsonArray[i].toString())
    }

    return result
}

fun JSONObject.toStringMap(): Map<String, String> {
    val result: MutableMap<String, String> = mutableMapOf()

    for (key in this.keys()) {
        result[key] = this.getString(key)
    }

    return result
}