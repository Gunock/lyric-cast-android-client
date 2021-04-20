/*
 * Created by Tomasz Kiljanczyk on 4/20/21 3:27 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 3:27 PM
 */

package pl.gunock.lyriccast.datamodel.documents.embedded

import io.realm.RealmObject
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(embedded = true)
open class LyricsSectionDocument(
    @field:Required
    var name: String,
    @field:Required
    var text: String
) : RealmObject() {

    constructor() : this("", "")

    override fun toString(): String {
        return "LyricsSectionDocument(name='$name', text='$text')"
    }

}
