/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/31/21 1:06 AM
 */

package pl.gunock.lyriccast.datamodel

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.gunock.lyriccast.datamodel.dao.CategoryDao
import pl.gunock.lyriccast.datamodel.dao.LyricsSectionDao
import pl.gunock.lyriccast.datamodel.dao.SetlistDao
import pl.gunock.lyriccast.datamodel.dao.SongDao
import pl.gunock.lyriccast.datamodel.entities.*

@Database(
    entities = [Song::class, LyricsSection::class, Category::class, Setlist::class,
        SongLyricsSectionCrossRef::class, SetlistSongCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class LyricCastRoomDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    abstract fun lyricsSectionDao(): LyricsSectionDao

    abstract fun categoryDao(): CategoryDao

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