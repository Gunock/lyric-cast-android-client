/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 1:02 AM
 */

package pl.gunock.lyriccast.datamodel.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import pl.gunock.lyriccast.datamodel.entities.LyricsSection
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.SongLyricsSectionCrossRef
import pl.gunock.lyriccast.datatransfer.models.SongDto

data class SongWithLyricsSections(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "songId",
        entityColumn = "songId"
    )
    val lyricsSections: List<LyricsSection>,
    @Relation(
        parentColumn = "songId",
        entityColumn = "songId"
    )
    val songLyricsSectionCrossRefs: List<SongLyricsSectionCrossRef>? = null
) {
    val crossRef get() = songLyricsSectionCrossRefs ?: listOf()

    fun lyricsSectionsToTextMap(): Map<Long, String> {
        return lyricsSections.distinct()
            .map { section -> section.id to section.text }
            .toMap()
    }

    fun lyricsSectionsToNameMap(): Map<Long, String> {
        return lyricsSections.distinct()
            .map { section -> section.id to section.name }
            .toMap()
    }

    fun toDto(category: String?): SongDto {
        val lyricsMap = lyricsSections.map { it.name to it.text }.toMap()

        val lyricsSectionNameMap = lyricsSectionsToNameMap()
        val presentation = songLyricsSectionCrossRefs?.sorted()
            ?.map { lyricsSectionNameMap[it.lyricsSectionId]!! }
            ?: listOf()

        return SongDto(song.title, lyricsMap, presentation, category ?: "")
    }

}