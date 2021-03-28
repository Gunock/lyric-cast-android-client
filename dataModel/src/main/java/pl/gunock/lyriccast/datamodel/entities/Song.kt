/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 1:54 AM
 */

package pl.gunock.lyriccast.datamodel.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pl.gunock.lyriccast.datamodel.extensions.toNonNullable
import pl.gunock.lyriccast.datamodel.extensions.toNullable

@Entity(indices = [Index(value = ["title"], unique = true)])
data class Song(
    @PrimaryKey(autoGenerate = true)
    val songId: Long? = null,
    var title: String = "",
    @ForeignKey(
        entity = Category::class,
        parentColumns = ["categoryId"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )
    var categoryId: Long? = null
) : Parcelable {
    val id: Long get() = songId.toNonNullable()

    constructor(parcel: Parcel) : this(
        parcel.readLong().toNullable(),
        parcel.readString()!!,
        parcel.readLong().toNullable()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(songId.toNonNullable())
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