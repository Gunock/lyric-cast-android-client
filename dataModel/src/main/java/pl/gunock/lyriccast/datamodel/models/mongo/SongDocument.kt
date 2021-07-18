/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 14:29
 */

package pl.gunock.lyriccast.datamodel.models.mongo

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.extentions.mapRealmList
import pl.gunock.lyriccast.datamodel.extentions.toRealmList
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.models.mongo.embedded.LyricsSectionDocument
import pl.gunock.lyriccast.datatransfer.models.SongDto

internal open class SongDocument(
    @field:Required
    var title: String = "",
    var lyrics: RealmList<LyricsSectionDocument> = RealmList(),
    var presentation: RealmList<String> = RealmList(),
    var category: CategoryDocument? = null,
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject() {

    @Ignore
    val idLong: Long = id.hashCode().toLong()

    @Ignore
    val lyricsMap: Map<String, String> = lyrics.map { it.name to it.text }.toMap()

    @Ignore
    val lyricsList: List<String> = presentation.map { lyricsMap[it]!! }

    constructor(dto: SongDto, category: CategoryDocument?) : this(
        dto.title,
        RealmList(),
        RealmList(),
        category,
        ObjectId()
    )

    constructor(document: SongDocument, id: ObjectId) : this(
        document.title,
        document.lyrics,
        document.presentation,
        document.category,
        id
    )

    constructor(song: Song) : this(
        id = ObjectId(song.id),
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

    fun toDto(): SongDto {
        return SongDto(title, lyricsMap, presentation.toList(), category?.name ?: "")
    }

    override fun toString(): String {
        return "SongDocument(title='$title', lyrics=$lyrics, presentation=$presentation, category=$category)"
    }

}