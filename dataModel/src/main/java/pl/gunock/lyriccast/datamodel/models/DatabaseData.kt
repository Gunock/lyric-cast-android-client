/*
 * Created by Tomasz Kiljanczyk on 4/4/21 2:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 12:46 AM
 */

package pl.gunock.lyriccast.datamodel.models

import org.json.JSONObject

data class DatabaseData(
    val songsJson: List<JSONObject>,
    val categoriesJson: List<JSONObject>,
    val setlistsJson: List<JSONObject>
)