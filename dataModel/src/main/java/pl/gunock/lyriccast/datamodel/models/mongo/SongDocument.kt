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
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.embedded.LyricsSectionDocument

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