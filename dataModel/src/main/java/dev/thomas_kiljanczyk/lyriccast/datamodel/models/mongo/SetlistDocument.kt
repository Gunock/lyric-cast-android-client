/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

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