/*
 * Created by Tomasz Kiljanczyk on 4/20/21 4:38 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 3:55 PM
 */

package pl.gunock.lyriccast.datamodel.documents

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datatransfer.models.CategoryDto

open class CategoryDocument(
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

    fun toDto(): CategoryDto {
        return CategoryDto(name, color)
    }

    override fun compareTo(other: CategoryDocument): Int {
        return name.compareTo(other.name)
    }

}