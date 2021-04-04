/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:02 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:45 AM
 */

package pl.gunock.lyriccast.datamodel.models

import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SetlistDto
import pl.gunock.lyriccast.datatransfer.models.SongDto

data class DatabaseTransferData(
    val songDtos: List<SongDto>?,
    val categoryDtos: List<CategoryDto>?,
    val setlistDtos: List<SetlistDto>?
)