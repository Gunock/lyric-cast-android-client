/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 4:41 PM
 */

package pl.gunock.lyriccast.datamodel.entities

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datatransfer.models.SetlistDto

// TODO: Implement
open class SetlistDocument(
    var name: String,
    var presentation: RealmList<ObjectId>,
    var songs: RealmList<SongDocument>,
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject() {

    @Ignore
    val idLong: Long = id.date.time

    fun toDto(): SetlistDto {
        val songMap = songs.map { it.id to it.title }.toMap()
        val songs: List<String> = presentation.map { songMap[it]!! }

        return SetlistDto(name, songs)
    }

    constructor() : this("", RealmList(), RealmList(), ObjectId())
}