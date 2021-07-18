/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
 */

package pl.gunock.lyriccast.datamodel.repositiories

import io.reactivex.Flowable
import pl.gunock.lyriccast.datamodel.models.Setlist

interface SetlistsRepository {

    fun getAllSetlists(): Flowable<List<Setlist>>

    fun getSetlist(id: String): Setlist?

    fun upsertSetlist(setlist: Setlist)

    fun deleteSetlists(setlistIds: Collection<String>)

}