/*
 * Created by Tomasz Kiljańczyk on 10/19/20 12:26 AM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/19/20 12:15 AM
 */

package pl.gunock.lyriccast.utils

import org.json.JSONArray
import org.json.JSONObject

object JsonHelper {

    fun arrayToStringList(jsonArray: JSONArray): List<String> {
        return List<String>(jsonArray.length()) {
            jsonArray.getString(it)
        }
    }

    fun arrayToJsonList(jsonArray: JSONArray): List<JSONObject> {
        return List<JSONObject>(jsonArray.length()) {
            jsonArray.getJSONObject(it)
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