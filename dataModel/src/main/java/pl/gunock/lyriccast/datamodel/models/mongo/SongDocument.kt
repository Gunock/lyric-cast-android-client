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
import pl.gunock.lyriccast.datamodel.extentions.toRealmList
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.embedded.LyricsSectionDocument

internal open class SongDocument(
    @field:Required
    var title: String = "",
    var lyrics: RealmList<LyricsSectionDocument> = RealmList(),
    var presentation: RealmList<String> = RealmList(),
    var category: CategoryDocument? = null,
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject() {

    constructor(song: Song) : this(
        id = if (song.id.isNotBlank()) ObjectId(song.id) else ObjectId(),
        title = song.title,
        lyrics = song.lyrics.mapRealmList { LyricsSectionDocument(it) },
        presentation = song.presentation.toRealmList(),
        category = song.category?.let { CategoryDocument(it) }
    )

    fun toGenericModel(): Song {
        return Song(
            id = id.toString(),
            title = title,
            lyrics = lyrics.map { it.toGenericModel() },
            presentation = presentation.toList(),
            category = category?.toGenericModel()
        )
    }

}