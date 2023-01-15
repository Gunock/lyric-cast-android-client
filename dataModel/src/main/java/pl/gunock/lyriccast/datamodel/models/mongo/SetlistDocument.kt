/*
 * Created by Tomasz Kiljanczyk on 12/11/2021, 18:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/11/2021, 17:59
 */

package pl.gunock.lyriccast.datamodel.models.mongo

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import pl.gunock.lyriccast.datamodel.models.Setlist

internal open class SetlistDocument() : RealmObject {
    @Suppress("PropertyName")
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var name: String = ""
    var presentation: RealmList<SongDocument> = realmListOf()

    constructor(setlist: Setlist) : this() {
        _id = if (setlist.id.isNotBlank()) ObjectId(setlist.id) else ObjectId()
        name = setlist.name
        presentation = setlist.presentation.map { SongDocument(it) }.toRealmList()
    }

    fun toGenericModel(): Setlist {
        return Setlist(
            id = _id.toHexString(),
            name = name,
            presentation = presentation.map { it.toGenericModel() }.toList()
        )
    }
}