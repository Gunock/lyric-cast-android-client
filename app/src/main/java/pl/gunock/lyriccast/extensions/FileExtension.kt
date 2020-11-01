/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/31/20 8:35 PM
 */

package pl.gunock.lyriccast.extensions

import org.json.JSONObject
import java.io.File

fun File.writeText(json: JSONObject) {
    return this.writeText(json.toString())
}

fun File.create(): File {
    File(this.parent!!).mkdirs()

    if (!this.createNewFile()) {
        this.writeText("")
    }

    return this
}