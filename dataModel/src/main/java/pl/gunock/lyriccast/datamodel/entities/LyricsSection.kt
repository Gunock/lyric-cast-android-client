/*
 * Created by Tomasz Kiljanczyk on 4/5/21 4:34 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 4:31 PM
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
}
