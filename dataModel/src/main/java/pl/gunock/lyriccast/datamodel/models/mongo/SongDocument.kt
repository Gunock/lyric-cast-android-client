/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
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
    // TODO: Verify if comparable is still needed

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