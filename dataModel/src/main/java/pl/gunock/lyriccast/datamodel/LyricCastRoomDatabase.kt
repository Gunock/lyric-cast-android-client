/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 5:11 PM
 */

package pl.gunock.lyriccast.datamodel

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.gunock.lyriccast.datamodel.dao.SetlistDao
import pl.gunock.lyriccast.datamodel.entities.LyricsSection
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.SetlistSongCrossRef
import pl.gunock.lyriccast.datamodel.entities.Song

@Database(
    entities = [Song::class, LyricsSection::class, Setlist::class, SetlistSongCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class LyricCastRoomDatabase : RoomDatabase() {

    abstract fun setlistDao(): SetlistDao

    companion object {
        @Volatile
        private var INSTANCE: LyricCastRoomDatabase? = null

        fun getDatabase(context: Context): LyricCastRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LyricCastRoomDatabase::class.java,
                    "lyric_cast_database"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}