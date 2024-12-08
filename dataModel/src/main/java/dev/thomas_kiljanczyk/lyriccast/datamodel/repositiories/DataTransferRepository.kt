/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.DatabaseTransferData
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.ImportOptions
import dev.thomas_kiljanczyk.lyriccast.datatransfer.models.SongDto
import kotlinx.coroutines.flow.Flow

interface DataTransferRepository {

    suspend fun clearDatabase()

    suspend fun importData(
        data: DatabaseTransferData,
        options: ImportOptions
    ): Flow<Int>

    suspend fun importSongs(
        songDtoSet: Set<SongDto>,
        options: ImportOptions
    ): Flow<Int>

    suspend fun getDatabaseTransferData(): DatabaseTransferData
}