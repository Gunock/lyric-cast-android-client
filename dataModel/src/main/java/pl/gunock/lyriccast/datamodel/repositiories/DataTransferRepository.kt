/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 22:40
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 20:13
 */

package pl.gunock.lyriccast.datamodel.repositiories

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.models.SongDto

interface DataTransferRepository {

    fun clearDatabase()

    fun importSongs(
        data: DatabaseTransferData,
        messageResourceId: MutableLiveData<Int>,
        options: ImportOptions
    )

    fun importSongs(
        songDtoSet: Set<SongDto>,
        messageResourceId: MutableLiveData<Int>,
        options: ImportOptions
    )

    fun getDatabaseTransferData(): DatabaseTransferData
}