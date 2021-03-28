/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 1:27 AM
 */

package pl.gunock.lyriccast.datamodel.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pl.gunock.lyriccast.datamodel.extensions.toNonNullable

@Entity(
    indices = [Index(value = ["songId"]), Index(value = ["lyricsSectionId"])]
)
data class SongLyricsSectionCrossRef(
    @PrimaryKey(autoGenerate = true)
    val songLyricsSectionCrossRefId: Long?,
    @ForeignKey(
        entity = Song::class,
        parentColumns = ["songId"],
        childColumns = ["songId"],
        onDelete = ForeignKey.CASCADE
    )
    val songId: Long,
    @ForeignKey(
        entity = LyricsSection::class,
        parentColumns = ["lyricsSectionId"],
        childColumns = ["lyricsSectionId"],
        onDelete = ForeignKey.CASCADE
    )
    val lyricsSectionId: Long,
    val order: Int
) : Comparable<SongLyricsSectionCrossRef> {
    val id: Long get() = songLyricsSectionCrossRefId.toNonNullable()

    override fun compareTo(other: SongLyricsSectionCrossRef): Int {
        return order.compareTo(other.order)
    }
}