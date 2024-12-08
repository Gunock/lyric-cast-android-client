/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.models.mongo

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

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