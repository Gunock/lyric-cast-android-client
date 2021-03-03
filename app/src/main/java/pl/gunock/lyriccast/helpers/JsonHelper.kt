/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:03 PM
 */

package pl.gunock.lyriccast.helpers

import org.json.JSONArray
import org.json.JSONObject

object JsonHelper {

    fun arrayToStringList(jsonArray: JSONArray): List<String> {
        return List<String>(jsonArray.length()) { position ->
            jsonArray.getString(position)
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