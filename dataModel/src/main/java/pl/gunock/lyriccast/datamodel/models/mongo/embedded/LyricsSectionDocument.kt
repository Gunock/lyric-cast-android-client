/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
 */

package pl.gunock.lyriccast.datamodel.models.mongo.embedded

import io.realm.kotlin.types.EmbeddedRealmObject
import pl.gunock.lyriccast.datamodel.models.Song

internal open class LyricsSectionDocument() : EmbeddedRealmObject {
    var name: String = ""
    var text: String = ""

    constructor(name: String, text: String) : this() {
        this.name = name
        this.text = text
    }

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
