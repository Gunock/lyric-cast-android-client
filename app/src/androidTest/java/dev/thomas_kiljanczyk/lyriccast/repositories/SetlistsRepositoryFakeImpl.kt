/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.repositories

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID
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
        val existingSetlist = setlists.find { it.id == setlist.id }
        if (existingSetlist != null) {
            setlists.remove(existingSetlist)
        } else {
            setlist.id = UUID.randomUUID().toString()
        }

        setlists += setlist
        setlistFlow.emit(setlists.toList())
    }

    override suspend fun deleteSetlists(setlistIds: Collection<String>) {
        setlists.removeIf { it.id in setlistIds }
        setlistFlow.emit(setlists.toList())
    }
}