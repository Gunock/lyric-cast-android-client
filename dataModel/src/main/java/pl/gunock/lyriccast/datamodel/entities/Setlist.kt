/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 2:24 AM
 */

package pl.gunock.lyriccast.datamodel.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import pl.gunock.lyriccast.datamodel.extensions.toNonNullable

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Setlist(
    @PrimaryKey(autoGenerate = true)
    val setlistId: Long?,
    val name: String
) : Parcelable {
    val id: Long get() = setlistId.toNonNullable()

    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(setlistId)
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