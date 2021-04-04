/*
 * Created by Tomasz Kiljanczyk on 4/4/21 2:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 1:32 AM
 */

package pl.gunock.lyriccast.datamodel.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pl.gunock.lyriccast.datamodel.extensions.toNonNullable

@Entity(
    indices = [Index(value = ["setlistId"]), Index(value = ["songId"])]
)
data class SetlistSongCrossRef(
    @PrimaryKey(autoGenerate = true)
    val setlistSongCrossRefId: Long? = null,
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
    val order: Int
) : Parcelable, Comparable<SetlistSongCrossRef> {
    val id: Long get() = setlistSongCrossRefId.toNonNullable()

    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt()
    )

    constructor(setlistId: Long, setlistSongCrossRef: SetlistSongCrossRef) : this(
        setlistSongCrossRef.setlistSongCrossRefId,
        setlistId,
        setlistSongCrossRef.songId,
        setlistSongCrossRef.order
    )


    override fun compareTo(other: SetlistSongCrossRef): Int {
        return order.compareTo(other.order)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(setlistId)
        parcel.writeLong(songId)
        parcel.writeInt(order)
        parcel.writeValue(setlistSongCrossRefId)
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
}