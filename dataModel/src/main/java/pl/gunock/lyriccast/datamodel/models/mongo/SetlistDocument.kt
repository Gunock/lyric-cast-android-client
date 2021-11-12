/*
 * Created by Tomasz Kiljanczyk on 12/11/2021, 18:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/11/2021, 17:59
 */

package pl.gunock.lyriccast.datamodel.models.mongo

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.extentions.mapRealmList
import pl.gunock.lyriccast.datamodel.models.Setlist

internal open class SetlistDocument(
    @field:Required
    var name: String = "",
    var presentation: RealmList<SongDocument> = RealmList(),
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject() {

    constructor(setlist: Setlist) : this(
        id = if (setlist.id.isNotBlank()) ObjectId(setlist.id) else ObjectId(),
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
}