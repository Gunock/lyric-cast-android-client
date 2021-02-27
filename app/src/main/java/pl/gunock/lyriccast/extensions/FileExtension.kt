/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:51 PM
 */

package pl.gunock.lyriccast.extensions

import org.json.JSONObject
import java.io.File

fun File.writeText(json: JSONObject) {
    this.writeText(json.toString())
}

fun File.create(): File {
    File(this.parent!!).mkdirs()

    if (!this.createNewFile()) {
        this.writeText("")
    }

    return this
}