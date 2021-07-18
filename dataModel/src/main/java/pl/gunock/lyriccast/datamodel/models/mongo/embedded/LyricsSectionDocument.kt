/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 14:04
 */

package pl.gunock.lyriccast.datamodel.models.mongo.embedded

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import io.realm.annotations.Required
import pl.gunock.lyriccast.datamodel.models.Song

@RealmClass(embedded = true)
open class LyricsSectionDocument(
    @field:Required
    var name: String,
    @field:Required
    var text: String
) : RealmObject() {

    constructor() : this("", "")

    constructor(lyricsSection: Song.LyricsSection) : this(lyricsSection.name, lyricsSection.text)

    fun toGenericModel(): Song.LyricsSection {
        return Song.LyricsSection(
            name = name,
            text = text
        )
    }

    override fun toString(): String {
        return "LyricsSectionDocument(name='$name', text='$text')"
    }

}
