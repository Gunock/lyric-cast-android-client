/*
 * Created by Tomasz Kiljanczyk on 16/05/2021, 17:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 15/05/2021, 18:00
 */

package pl.gunock.lyriccast.common.tests

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runners.Parameterized
import pl.gunock.lyriccast.common.extensions.normalize

// TODO: Fix the tests
class StringsExtensionsTest {

    companion object {
        @Suppress("SpellCheckingInspection")
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Iterable<Array<String>> {
            return listOf(
                arrayOf("zażółć gęślą jaźń", "zazolc gesla jazn"),
                arrayOf("ZAŻÓŁĆ GĘŚLĄ JAŹŃ", "ZAZOLC GESLA JAZN"),
                arrayOf("ZażóŁĆ GęŚLĄ JaŹń", "ZazoLC GeSLA JaZn")
            )
        }
    }

    @Test
    fun stringIsNormalized(text: String, normalizedText: String) {
        assertThat(text.normalize()).isEqualTo(normalizedText)
    }

}