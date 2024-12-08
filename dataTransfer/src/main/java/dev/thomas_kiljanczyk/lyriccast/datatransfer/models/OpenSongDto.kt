/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.datatransfer.models

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
            presentationFixed.uppercase()
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

            result[sectionName.uppercase()] = sectionText
        }

        return result
    }
}