/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 19:57
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 12/11/2022, 19:57
 */

package pl.gunock.lyriccast.common.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.common.helpers.FileHelper
import java.io.File

@RunWith(AndroidJUnit4::class)
class FileHelperTest {

    private companion object {
        const val testFileContent = "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    }

    private lateinit var testDir: File
    private lateinit var testFile: File
    private lateinit var testZipFile: File

    @Before
    fun setUp() {
        testDir = File("FileHelperTest")
        testDir.deleteRecursively()
        testDir.mkdirs()

        testFile = File("${testDir.path}/FileHelperTest.txt")
        testZipFile = File("${testDir.path}/FileHelperTest.zip")

        testFile.createNewFile()
        testFile.writeText(testFileContent)
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
        assertThat(testFile.readText()).isEqualTo(testFileContent)
    }

}