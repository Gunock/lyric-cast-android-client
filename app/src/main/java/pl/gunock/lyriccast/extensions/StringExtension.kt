/*
 * Created by Tomasz Kiljańczyk on 3/7/21 11:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/7/21 3:15 PM
 */

package pl.gunock.lyriccast.extensions

import java.text.Normalizer

private val nonSpacingMarkRegex = "\\p{Mn}+".toRegex()

fun String.normalize(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD).replace(nonSpacingMarkRegex, "")
}