/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 13:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 13:05
 */

package pl.gunock.lyriccast.datamodel.repositiories

import kotlinx.coroutines.flow.Flow
import pl.gunock.lyriccast.datamodel.models.Setlist

interface SetlistsRepository {

    fun getAllSetlists(): Flow<List<Setlist>>

    fun getSetlist(id: String): Setlist?

    suspend fun upsertSetlist(setlist: Setlist)

    suspend fun deleteSetlists(setlistIds: Collection<String>)

}