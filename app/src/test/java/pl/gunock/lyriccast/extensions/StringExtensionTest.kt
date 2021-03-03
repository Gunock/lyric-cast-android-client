/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:09 PM
 */

package pl.gunock.lyriccast.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@Suppress("SpellCheckingInspection")
class StringExtensionTest {
    @ParameterizedTest
    @CsvSource(
        "ąćęłńóśźż, acelnoszz",
        "ĄĆĘŁŃÓŚŹŻ, ACELNOSZZ"
    )
    fun normalize_isCorrect(source: String, expected: String) {
        assertEquals(expected, source.normalize())
    }
}