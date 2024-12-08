/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.common.tests

import com.google.common.truth.Truth.assertThat
import dev.thomas_kiljanczyk.lyriccast.common.extensions.normalize
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class StringsExtensionsTest(private val text: String, private val normalizedText: String) {

    companion object {
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
    fun stringIsNormalized() {
        assertThat(text.normalize()).isEqualTo(normalizedText)
    }

}