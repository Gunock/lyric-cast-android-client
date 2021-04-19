/*
 * Created by Tomasz Kiljanczyk on 4/20/21 1:10 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 12:11 AM
 */

package pl.gunock.lyriccast.datamodel.documents

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
