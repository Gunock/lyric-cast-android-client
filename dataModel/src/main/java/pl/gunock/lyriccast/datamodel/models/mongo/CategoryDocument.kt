/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 20:10
 */

package pl.gunock.lyriccast.datamodel.models.mongo

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datatransfer.models.CategoryDto

internal open class CategoryDocument(
    @field:Required
    var name: String,
    var color: Int? = null,
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject(), Comparable<CategoryDocument> {

    @Ignore
    val idLong: Long = id.hashCode().toLong()

    constructor() : this("", null, ObjectId())

    constructor(dto: CategoryDto) : this(dto.name, dto.color, ObjectId())

    constructor(document: CategoryDocument, id: ObjectId) : this(document.name, document.color, id)

    constructor(category: Category) : this(category.name, category.color, ObjectId(category.id))

    fun toGenericModel(): Category {
        return Category(
            id = id.toString(),
            name = name,
            color = color
        )
    }

    fun toDto(): CategoryDto {
        return CategoryDto(name, color)
    }

    override fun compareTo(other: CategoryDocument): Int {
        return name.compareTo(other.name)
    }

}