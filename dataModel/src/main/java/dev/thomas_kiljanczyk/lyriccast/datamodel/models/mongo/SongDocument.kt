/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo.embedded.LyricsSectionDocument
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

internal open class SongDocument() : RealmObject {
    @Suppress("PropertyName")
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var title: String = ""
    var lyrics: RealmList<LyricsSectionDocument> = realmListOf()
    var presentation: RealmList<String> = realmListOf()
    var category: CategoryDocument? = null

    constructor(song: Song) : this() {
        _id = if (song.id.isNotBlank()) ObjectId(song.id) else ObjectId()
        title = song.title
        lyrics = song.lyrics.map { LyricsSectionDocument(it) }.toRealmList()
        presentation = song.presentation.toRealmList()
        category = song.category?.let { CategoryDocument(it) }
    }

    fun toGenericModel(): Song {
        return Song(
            id = _id.toHexString(),
            title = title,
            lyrics = lyrics.map { it.toGenericModel() },
            presentation = presentation.toList(),
            category = category?.toGenericModel()
        )
    }

}