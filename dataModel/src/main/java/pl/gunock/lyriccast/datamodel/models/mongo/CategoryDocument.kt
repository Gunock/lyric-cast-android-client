/*
 * Created by Tomasz Kiljanczyk on 12/11/2021, 18:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/11/2021, 17:59
 */

package pl.gunock.lyriccast.datamodel.models.mongo

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import pl.gunock.lyriccast.datamodel.models.Category

internal open class CategoryDocument() : RealmObject, Comparable<CategoryDocument> {
    @Suppress("PropertyName")
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var name: String = ""
    var color: Int? = null

    constructor(category: Category) : this() {
        _id = if (category.id.isNotBlank()) ObjectId(category.id) else ObjectId()
        name = category.name
        color = category.color
    }

    fun toGenericModel(): Category {
        return Category(
            id = _id.toHexString(),
            name = name,
            color = color
        )
    }

    override fun compareTo(other: CategoryDocument): Int {
        return name.compareTo(other.name)
    }

}