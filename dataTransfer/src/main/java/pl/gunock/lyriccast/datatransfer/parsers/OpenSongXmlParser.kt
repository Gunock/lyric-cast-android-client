/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 1:02 AM
 */

package pl.gunock.lyriccast.datatransfer.parsers

import android.content.ContentResolver
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datatransfer.models.OpenSongDto
import pl.gunock.lyriccast.datatransfer.models.SongDto
import java.io.File
import java.io.InputStream
import java.util.*

internal class OpenSongXmlParser(filesDir: File) : ImportSongXmlParser(filesDir) {

    override fun parseZip(resolver: ContentResolver, inputStream: InputStream): Set<SongDto> {
        importDirectory.deleteRecursively()
        importDirectory.mkdirs()
        FileHelper.unzip(resolver, inputStream, importDirectory.canonicalPath)

        val fileList1 = importDirectory.listFiles() ?: arrayOf()
        val result: MutableSet<SongDto> = mutableSetOf()
        for (file1 in fileList1) {
            if (!file1.isDirectory) {
                result.add(parse(file1.inputStream()))
                continue
            }

            val category = file1.name
            val fileList2 = file1.listFiles() ?: arrayOf()
            for (file2 in fileList2) {
                if (file2.isDirectory) {
                    continue
                }
                val song = parse(file2.inputStream(), category)
                result.add(song)
            }
        }

        importDirectory.deleteRecursively()
        importDirectory.mkdirs()
        return result
    }

    override fun parse(inputStream: InputStream?, category: String): SongDto {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setInput(inputStream, null)
        parser.nextTag()
        parser.require(XmlPullParser.START_TAG, null, "song")

        val song = readSong(parser)

        val presentationList: List<String> = if (song.presentationList.isNotEmpty()) {
            song.presentationList
        } else {
            song.lyricsMap.keys.toList()
        }

        return SongDto(
            title = song.title,
            presentation = presentationList,
            lyrics = song.lyricsMap,
            category = category.trim().toUpperCase(Locale.getDefault())
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