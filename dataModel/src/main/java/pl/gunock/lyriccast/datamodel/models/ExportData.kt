/*
 * Created by Tomasz Kiljanczyk on 4/4/21 12:28 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/3/21 11:47 PM
 */

package pl.gunock.lyriccast.datamodel.models

import org.json.JSONObject

data class ExportData(
    val songsJson: List<JSONObject>,
    val categoriesJson: List<JSONObject>,
    val setlistsJson: List<JSONObject>
)