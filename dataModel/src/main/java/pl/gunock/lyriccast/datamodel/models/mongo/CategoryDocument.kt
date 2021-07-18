/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
 */

package pl.gunock.lyriccast.datamodel.models.mongo

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datamodel.models.Category

internal open class CategoryDocument(
    @field:Required
    var name: String = "",
    var color: Int? = null,
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject(), Comparable<CategoryDocument> {
    // TODO: Verify if comparable is still needed

    constructor(category: Category) : this(
        id = if (category.id.isNotBlank()) ObjectId(category.id) else ObjectId(),
        name = category.name,
        color = category.color
    )

    fun toGenericModel(): Category {
        return Category(
            id = id.toString(),
            name = name,
            color = color
        )
    }

    override fun compareTo(other: CategoryDocument): Int {
        return name.compareTo(other.name)
    }

}