/*
 * Created by Tomasz Kiljanczyk on 29/12/2021, 14:52
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/12/2021, 12:25
 */

package pl.gunock.lyriccast.datamodel.repositiories

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.models.SongDto

interface DataTransferRepository {

    suspend fun clearDatabase()

    suspend fun importSongs(
        data: DatabaseTransferData,
        messageResourceId: MutableLiveData<Int>,
        options: ImportOptions
    )

    suspend fun importSongs(
        songDtoSet: Set<SongDto>,
        messageResourceId: MutableLiveData<Int>,
        options: ImportOptions
    )

    suspend fun getDatabaseTransferData(): DatabaseTransferData
}