/*
 * Created by Tomasz Kiljańczyk on 10/14/20 11:51 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/14/20 8:33 PM
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

        json.keys().forEach { key ->
            result[key] = json[key] as String
        }

        return result
    }

}