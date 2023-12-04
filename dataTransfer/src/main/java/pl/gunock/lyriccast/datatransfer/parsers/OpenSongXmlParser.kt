/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 19:57
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 22/09/2022, 20:49
 */

package pl.gunock.lyriccast.datatransfer.parsers

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datatransfer.models.OpenSongDto
import pl.gunock.lyriccast.datatransfer.models.SongDto
import java.io.File
import java.io.InputStream

internal class OpenSongXmlParser(filesDir: File) : ImportSongXmlParser(filesDir) {

    companion object {
        const val TAG = "OpenSongXmlParser"
    }

    override fun parseZip(inputStream: InputStream): Set<SongDto> {
        importDirectory.deleteRecursively()
        importDirectory.mkdirs()
        FileHelper.unzip(inputStream, importDirectory.canonicalPath)

        val fileList1 = importDirectory.listFiles() ?: arrayOf()
        val result: MutableSet<SongDto> = mutableSetOf()
        for (file1 in fileList1) {
            if (!file1.isDirectory) {
                Log.v(TAG, file1.canonicalPath)
                file1.inputStream().use { result.add(parse(it)) }
                continue
            }

            val category = file1.name
            val fileList2 = file1.listFiles() ?: arrayOf()
            for (file2 in fileList2) {
                if (file2.isDirectory) {
                    continue
                }
                Log.v(TAG, "Parsing file at ${file2.canonicalPath}")
                val song = file2.inputStream().use { parse(it, category) }
                result.add(song)
            }
        }

        importDirectory.deleteRecursively()
        return result
    }

    override fun parse(inputStream: InputStream?, category: String): SongDto {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setInput(inputStream, null)
        try {
            parser.nextTag()
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Encountered error for song in '${category}' category")
            throw e
        }
        parser.require(XmlPullParser.START_TAG, null, "song")

        val song = readSong(parser)

        val presentationList: List<String> = song.presentationList.ifEmpty {
            song.lyricsMap.keys.toList()
        }

        return SongDto(
            title = song.title,
            presentation = presentationList,
            lyrics = song.lyricsMap,
            category = category.trim().uppercase()
        )
    }

    private fun readSong(parser: XmlPullParser): OpenSongDto {
        var title = ""
        var presentation = ""
        var lyrics = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "title" -> title = readTag(parser, "title")
                "presentation" -> presentation = readTag(parser, "presentation")
                "lyrics" -> lyrics = readTag(parser, "lyrics")
                else -> skip(parser)
            }
        }
        return OpenSongDto(title, presentation, lyrics)
    }

    private fun readTag(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, null, tag)
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, null, tag)
        return result.trim()
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}