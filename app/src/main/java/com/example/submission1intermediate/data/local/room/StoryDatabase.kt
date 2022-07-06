package com.example.submission1intermediate.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.submission1intermediate.data.local.entity.RemoteKeys
import com.example.submission1intermediate.data.local.entity.StoryEntity

@Database(
    entities = [StoryEntity::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)

abstract class StoryDatabase: RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object{

        @Volatile
        private var instance: StoryDatabase? = null

        fun getInstance(context: Context): StoryDatabase{
            if (instance == null){
                synchronized(StoryDatabase::class.java){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        StoryDatabase::class.java, "StoryApps.db").build()
                }
            }
            return instance as StoryDatabase
        }
    }
}