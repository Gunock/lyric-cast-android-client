/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.common.tests

import com.google.common.truth.Truth.assertThat
import dev.thomas_kiljanczyk.lyriccast.common.helpers.FileHelper
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.File


// TODO: Fix the tests
class FileHelperTest {

    private companion object {
        const val TEST_FILE_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    }

    private lateinit var testDir: File
    private lateinit var testFile: File
    private lateinit var testZipFile: File

    @Before
    fun setup() {
        testDir = File("FileHelperTest")
        testDir.deleteRecursively()
        testDir.mkdirs()

        testFile = File("${testDir.path}/FileHelperTest.txt")
        testZipFile = File("${testDir.path}/FileHelperTest.zip")

        testFile.createNewFile()
        testFile.writeText(TEST_FILE_CONTENT)
    }

    @After
    fun cleanUp() {
        try {
            testDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun fileIsZippedAndUnzipped() {
        val zipSuccessful = FileHelper.zip(testZipFile.outputStream(), testFile.parent!!)

        assertThat(zipSuccessful).isTrue()
        assertThat(testZipFile.exists()).isTrue()
        assertThat(testZipFile.readBytes().size).isGreaterThan(0)
        testFile.delete()

        try {
            FileHelper.unzip(testZipFile.inputStream(), testFile.parent!!)
        } catch (e: Exception) {
            e.printStackTrace()
            fail("FileHelper.unzip failed.")
        }

        assertThat(testFile.exists()).isTrue()
        assertThat(testFile.readText()).isEqualTo(TEST_FILE_CONTENT)
    }

}