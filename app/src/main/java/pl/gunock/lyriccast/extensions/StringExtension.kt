/*
 * Created by Tomasz Kiljańczyk on 2/27/21 8:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 8:44 PM
 */

package pl.gunock.lyriccast.extensions

import java.text.Normalizer

val nonSpacingMarkRegex = "\\p{Mn}+".toRegex()

fun String.normalize(): String {
    var result: String = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(nonSpacingMarkRegex, "")

    return result
}