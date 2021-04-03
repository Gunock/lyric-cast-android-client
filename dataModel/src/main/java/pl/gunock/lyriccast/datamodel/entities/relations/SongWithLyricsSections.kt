/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 3:18 AM
 */

package pl.gunock.lyriccast.datamodel.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import pl.gunock.lyriccast.datamodel.entities.LyricsSection
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.SongLyricsSectionCrossRef

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

}