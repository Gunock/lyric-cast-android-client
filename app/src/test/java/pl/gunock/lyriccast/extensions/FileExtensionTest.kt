/*
 * Created by Tomasz Kiljańczyk on 11/1/20 3:44 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 11/1/20 3:43 PM
 */

package pl.gunock.lyriccast.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class FileExtensionTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            File("test").deleteRecursively()
        }
    }

    @Test
    fun create_isCreated() {
        val file = File("test/test1/test.txt")

        assertEquals(false, file.exists())
        file.create()
        assertEquals(true, file.exists())
    }
}