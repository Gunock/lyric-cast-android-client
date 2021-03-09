/*
 * Created by Tomasz Kiljańczyk on 3/9/21 2:21 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/9/21 1:22 AM
 */

package pl.gunock.lyriccast.extensions

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.getIntOrNull(name: String): Int? {
    return if (this.isNull(name)) null else this.getInt(name)
}

fun JSONObject.getStringArray(name: String): Array<String> {
    val jsonArray: JSONArray = this.getJSONArray(name)
    return Array(jsonArray.length()) { position ->
        jsonArray.getString(position)
    }
}

fun JSONObject.getLongArray(name: String): Array<Long> {
    val jsonArray: JSONArray = this.getJSONArray(name)
    return Array(jsonArray.length()) { position ->
        jsonArray.getLong(position)
    }
}

fun JSONObject.toMap(): Map<String, String> {
    val result: HashMap<String, String> = HashMap()

    for (key in this.keys()) {
        result[key] = this[key] as String
    }

    return result
}
