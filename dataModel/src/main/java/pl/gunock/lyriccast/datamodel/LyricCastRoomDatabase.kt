/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/27/21 11:45 PM
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
                    "word_database"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}