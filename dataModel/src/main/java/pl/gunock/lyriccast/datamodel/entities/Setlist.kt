/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 1:28 AM
 */

package pl.gunock.lyriccast.datamodel.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import pl.gunock.lyriccast.datamodel.extensions.toNonNullable
import pl.gunock.lyriccast.datamodel.extensions.toNullable

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Setlist(
    @PrimaryKey(autoGenerate = true)
    val setlistId: Long?,
    val name: String
) : Parcelable {
    val id: Long get() = setlistId.toNonNullable()

    constructor(parcel: Parcel) : this(
        parcel.readLong().toNullable(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(setlistId.toNonNullable())
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Setlist> {
        override fun createFromParcel(parcel: Parcel): Setlist {
            return Setlist(parcel)
        }

        override fun newArray(size: Int): Array<Setlist?> {
            return arrayOfNulls(size)
        }
    }
}