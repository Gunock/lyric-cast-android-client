/*
 * Created by Tomasz Kiljanczyk on 06/05/2021, 13:47
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/05/2021, 13:47
 */

package pl.gunock.lyriccast.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import pl.gunock.lyriccast.common.extensions.normalize

@RunWith(Parameterized::class)
class StringsExtensionsTest(
    private val text: String,
    private val normalizedText: String
) {

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
    fun stringIsNormalized() {
        assertThat(text.normalize()).isEqualTo(normalizedText)
    }

}