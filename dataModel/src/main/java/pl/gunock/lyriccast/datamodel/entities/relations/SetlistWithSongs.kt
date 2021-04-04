/*
 * Created by Tomasz Kiljanczyk on 4/4/21 12:28 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 12:12 AM
 */

package pl.gunock.lyriccast.datamodel.entities.relations

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.json.JSONObject
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.SetlistSongCrossRef
import pl.gunock.lyriccast.datamodel.entities.Song

data class SetlistWithSongs(
    @Embedded val setlist: Setlist,
    @Relation(
        parentColumn = "setlistId",
        entityColumn = "songId",
        associateBy = Junction(SetlistSongCrossRef::class)
    )
    val songs: List<Song>,
    @Relation(
        parentColumn = "setlistId",
        entityColumn = "setlistId"
    )
    val setlistSongCrossRefs: List<SetlistSongCrossRef>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Setlist::class.java.classLoader)!!,
        parcel.createTypedArrayList(Song)?.toList() ?: listOf(),
        parcel.createTypedArrayList(SetlistSongCrossRef)?.toList() ?: listOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(setlist, flags)
        parcel.writeTypedList(songs)
        parcel.writeTypedList(setlistSongCrossRefs)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun toJson(): JSONObject {
        val songMap = songs.map { it.songId to it.title }.toMap()

        val songs: List<String> = setlistSongCrossRefs.sorted().map { songMap[it.songId]!! }

        return JSONObject().apply {
            put("name", setlist.name)
            put("songs", JSONObject.wrap(songs))
        }
    }

    companion object CREATOR : Parcelable.Creator<SetlistWithSongs> {
        override fun createFromParcel(parcel: Parcel): SetlistWithSongs {
            return SetlistWithSongs(parcel)
        }

        override fun newArray(size: Int): Array<SetlistWithSongs?> {
            return arrayOfNulls(size)
        }
    }

}