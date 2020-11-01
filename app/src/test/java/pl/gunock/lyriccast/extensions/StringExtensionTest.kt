/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/31/20 9:05 PM
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