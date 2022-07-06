package com.example.submission1intermediate.data.local.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.submission1intermediate.data.local.entity.StoryEntity

@Dao
interface StoryDao {

    //insert storyEntity ke database lokal
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(vararg storyEntity: StoryEntity)

    @Query("SELECT * FROM story")
    fun getAllStories(): PagingSource<Int, StoryEntity>

    @Query("DELETE FROM story")
    fun deleteAll()

}