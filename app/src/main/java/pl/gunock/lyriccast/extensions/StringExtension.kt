/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/31/20 8:59 PM
 */

package pl.gunock.lyriccast.extensions

import java.text.Normalizer

private val specialCharacters: Map<Char, Char> = mapOf(
    'ł' to 'l',
    'Ł' to 'L',
)

val nonSpacingMarkRegex = "\\p{Mn}+".toRegex()

fun String.normalize(): String {
    var result: String = Normalizer.normalize(this, Normalizer.Form.NFD)
//        .replace(nonSpacingMarkRegex, "")

    for (character in specialCharacters) {
        result = result.replace(character.key, character.value)
    }

    return result
}