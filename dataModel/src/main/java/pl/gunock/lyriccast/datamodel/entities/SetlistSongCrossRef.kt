/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 3:05 AM
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

@Entity(
    indices = [Index(value = ["setlistId"]), Index(value = ["songId"])]
)
data class SetlistSongCrossRef(
    @ForeignKey(
        entity = Setlist::class,
        parentColumns = ["setlistId"],
        childColumns = ["setlistId"],
        onDelete = ForeignKey.CASCADE
    )
    val setlistId: Long,
    @ForeignKey(
        entity = Song::class,
        parentColumns = ["songId"],
        childColumns = ["songId"],
        onDelete = ForeignKey.CASCADE
    )
    val songId: Long,
    val order: Int,
    @PrimaryKey(autoGenerate = true)
    val setlistSongCrossRefId: Long? = null
) : Parcelable, Comparable<SetlistSongCrossRef> {
    val id: Long get() = setlistSongCrossRefId.toNonNullable()

    constructor(setlistId: Long, setlistSongCrossRef: SetlistSongCrossRef) : this(
        setlistId,
        setlistSongCrossRef.songId,
        setlistSongCrossRef.order,
        setlistSongCrossRef.setlistSongCrossRefId
    )

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readLong().toNullable()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(setlistSongCrossRefId.toNonNullable())
        parcel.writeLong(setlistId)
        parcel.writeLong(songId)
        parcel.writeInt(order)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SetlistSongCrossRef> {
        override fun createFromParcel(parcel: Parcel): SetlistSongCrossRef {
            return SetlistSongCrossRef(parcel)
        }

        override fun newArray(size: Int): Array<SetlistSongCrossRef?> {
            return arrayOfNulls(size)
        }
    }

    override fun compareTo(other: SetlistSongCrossRef): Int {
        return order.compareTo(other.order)
    }
}