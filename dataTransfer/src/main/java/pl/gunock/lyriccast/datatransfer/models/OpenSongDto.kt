/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:33 AM
 */

package pl.gunock.lyriccast.datatransfer.models

import java.util.*

internal data class OpenSongDto(
    val title: String,
    val presentation: String,
    val lyrics: String
) {
    private companion object {
        val LYRICS_REGEX = "\\[([^]]+)]([^\\[<]+)".toRegex()
        val FIX_PRESENTATION_REGEX_1 = "([^ ]+) +([0-9]+)".toRegex()
        val FIX_PRESENTATION_REGEX_2 = "([^0-9 ]+[0-9]+)([^0-9 ])".toRegex()
        val SPLIT_SPACE_REGEX = " +".toRegex()
        val LYRICS_SPACE_REGEX = "  +".toRegex()
    }

    val presentationList: List<String>
    val lyricsMap: Map<String, String>

    init {
        lyricsMap = createLyricsMap()
        presentationList = if (presentation.isNotBlank()) {
            var presentationFixed = FIX_PRESENTATION_REGEX_1.replace(presentation) { match ->
                match.groupValues[1] + match.groupValues[2]
            }
            presentationFixed = FIX_PRESENTATION_REGEX_2.replace(presentationFixed) { match ->
                "${match.groupValues[1]} ${match.groupValues[2]}"
            }
            presentationFixed.toUpperCase(Locale.getDefault())
                .split(SPLIT_SPACE_REGEX)
                .filter { lyricsMap.containsKey(it) }
        } else {
            lyricsMap.keys.toList()
        }
    }

    private fun createLyricsMap(): Map<String, String> {
        if (!lyrics.contains("\\[".toRegex())) {
            return mapOf("Unison" to lyrics)
        }
        val regexResults = LYRICS_REGEX.findAll(lyrics.replace(LYRICS_SPACE_REGEX, " "))
        val result = mutableMapOf<String, String>()
        for (regexResult: MatchResult in regexResults) {
            val sectionName: String = regexResult.groupValues[1]
            val sectionText: String = regexResult.groupValues[2]
                .trim()
                .replace("\n +".toRegex(), "\n")

            result[sectionName.toUpperCase(Locale.getDefault())] = sectionText
        }

        return result
    }
}