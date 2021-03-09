/*
 * Created by Tomasz Kiljańczyk on 3/9/21 1:07 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/8/21 11:52 PM
 */

package pl.gunock.lyriccast.helpers

import org.json.JSONArray
import org.json.JSONObject

object JsonHelper {

    fun arrayToStringList(jsonArray: JSONArray): List<String> {
        return List(jsonArray.length()) { position ->
            jsonArray.getString(position)
        }
    }

    fun arrayToLongList(jsonArray: JSONArray): List<Long> {
        return List(jsonArray.length()) { position ->
            jsonArray.getLong(position)
        }
    }

    fun objectToMap(json: JSONObject): Map<String, String> {
        val result: HashMap<String, String> = HashMap()

        for (key in json.keys()) {
            result[key] = json[key] as String
        }

        return result
    }

}