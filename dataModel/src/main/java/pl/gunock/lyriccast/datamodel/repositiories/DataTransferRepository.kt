/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 13:05
 */

package pl.gunock.lyriccast.datamodel.repositiories

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.models.SongDto
import java.io.Closeable

interface DataTransferRepository : Closeable {

    fun clearDatabase()

    fun importSongs(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    )

    fun importSongs(
        songDtoSet: Set<SongDto>,
        message: MutableLiveData<String>,
        options: ImportOptions
    )

    fun getDatabaseTransferData(): DatabaseTransferData
}