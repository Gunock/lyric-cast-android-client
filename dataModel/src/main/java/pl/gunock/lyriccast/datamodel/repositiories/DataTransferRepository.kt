/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 17:30
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 17:29
 */

package pl.gunock.lyriccast.datamodel.repositiories

import kotlinx.coroutines.flow.Flow
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.models.SongDto

interface DataTransferRepository {

    suspend fun clearDatabase()

    suspend fun importSongs(
        data: DatabaseTransferData,
        options: ImportOptions
    ): Flow<Int>

    suspend fun importSongs(
        songDtoSet: Set<SongDto>,
        options: ImportOptions
    ): Flow<Int>

    suspend fun getDatabaseTransferData(): DatabaseTransferData
}