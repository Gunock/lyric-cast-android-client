/*
 * Created by Tomasz Kiljanczyk on 4/4/21 2:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 2:00 AM
 */

package pl.gunock.lyriccast.datamodel.extensions

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