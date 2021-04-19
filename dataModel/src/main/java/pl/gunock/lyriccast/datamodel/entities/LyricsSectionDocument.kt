/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 4:41 PM
 */

package pl.gunock.lyriccast.datamodel.entities

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
