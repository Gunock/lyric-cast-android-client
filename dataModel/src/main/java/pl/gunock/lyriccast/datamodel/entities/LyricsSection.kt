/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 1:48 AM
 */

package pl.gunock.lyriccast.datamodel.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import pl.gunock.lyriccast.datamodel.extensions.toNonNullable

@Entity
data class LyricsSection(
    @PrimaryKey(autoGenerate = true)
    val lyricsSectionId: Long? = null,
    @ForeignKey(
        entity = Song::class,
        parentColumns = ["songId"],
        childColumns = ["songId"],
        onDelete = ForeignKey.CASCADE
    )
    val songId: Long,
    val name: String,
    val text: String
) {
    val id: Long get() = lyricsSectionId.toNonNullable()

    constructor(songId: Long, lyricsSection: LyricsSection) : this(
        lyricsSection.lyricsSectionId,
        songId,
        lyricsSection.name,
        lyricsSection.text
    )
}
