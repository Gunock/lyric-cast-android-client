/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 19:35
 */

package pl.gunock.lyriccast.datamodel.repositiories

import io.reactivex.Flowable
import pl.gunock.lyriccast.datamodel.models.Setlist
import java.io.Closeable

interface SetlistsRepository : Closeable {

    fun getAllSetlists(): Flowable<List<Setlist>>

    fun getSetlist(id: String): Setlist?

    fun upsertSetlist(setlist: Setlist)

    fun deleteSetlists(setlistIds: Collection<String>)

}