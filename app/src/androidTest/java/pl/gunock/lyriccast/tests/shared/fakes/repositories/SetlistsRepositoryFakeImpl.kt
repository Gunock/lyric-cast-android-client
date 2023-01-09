/*
 * Created by Tomasz Kiljanczyk on 08/01/2023, 23:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 08/01/2023, 23:51
 */

package pl.gunock.lyriccast.tests.shared.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import javax.inject.Inject

class SetlistsRepositoryFakeImpl @Inject constructor() : SetlistsRepository {
    private val setlists = mutableListOf<Setlist>()
    private val setlistFlow = MutableStateFlow(setlists.toList())


    override fun getAllSetlists(): Flow<List<Setlist>> {
        return setlistFlow
    }

    override fun getSetlist(id: String): Setlist? {
        return setlists.firstOrNull { it.id == id }
    }

    override suspend fun upsertSetlist(setlist: Setlist) {
        setlists += setlist
        setlistFlow.emit(setlists.toList())
    }

    override suspend fun deleteSetlists(setlistIds: Collection<String>) {
        setlists.removeIf { it.id in setlistIds }
        setlistFlow.emit(setlists.toList())
    }
}