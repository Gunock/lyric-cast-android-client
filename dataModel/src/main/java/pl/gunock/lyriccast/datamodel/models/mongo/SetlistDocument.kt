/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 14:24
 */

package pl.gunock.lyriccast.datamodel.models.mongo

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.extentions.mapRealmList
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datatransfer.models.SetlistDto

internal open class SetlistDocument(
    @field:Required
    var name: String = "",
    var presentation: RealmList<SongDocument> = RealmList(),
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject() {

    @Ignore
    val idLong: Long = id.hashCode().toLong()

//    constructor() : this("", RealmList(), ObjectId())

    constructor(dto: SetlistDto) : this(dto.name, RealmList(), ObjectId())

    constructor(document: SetlistDocument, id: ObjectId) : this(
        document.name,
        document.presentation,
        id
    )

    constructor(setlist: Setlist) : this(
        id = ObjectId(setlist.id),
        name = setlist.name,
        presentation = setlist.presentation.mapRealmList { SongDocument(it) }
    )

    fun toGenericModel(): Setlist {
        return Setlist(
            id = id.toString(),
            name = name,
            presentation = presentation.map { it.toGenericModel() }.toList()
        )
    }

    fun toDto(): SetlistDto {
        val songs: List<String> = presentation.map { it.title }
        return SetlistDto(name, songs)
    }
}