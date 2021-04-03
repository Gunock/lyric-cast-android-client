/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 3:18 AM
 */

package pl.gunock.lyriccast.datamodel.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.Song

data class SongAndCategory(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val category: Category?
)