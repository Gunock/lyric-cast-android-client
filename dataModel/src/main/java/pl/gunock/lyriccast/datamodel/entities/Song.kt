/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 5:11 PM
 */

package pl.gunock.lyriccast.datamodel.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import pl.gunock.lyriccast.datamodel.extensions.toNonNullable
import pl.gunock.lyriccast.datatransfer.models.SongDto

@Entity(indices = [Index(value = ["title"], unique = true)])
data class Song(
    @PrimaryKey(autoGenerate = true)
    val songId: Long? = null,
    var title: String = "",
    var categoryId: Long? = null
) : Parcelable {
    val id: Long get() = songId.toNonNullable()

    constructor(songDto: SongDto, categoryId: Long?) : this(
        null,
        songDto.title,
        categoryId
    )

    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString()!!,
        parcel.readValue(Long::class.java.classLoader) as? Long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(songId)
        parcel.writeString(title)
        parcel.writeValue(categoryId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }

}