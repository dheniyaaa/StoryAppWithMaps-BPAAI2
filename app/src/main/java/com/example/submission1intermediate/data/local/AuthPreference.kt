package com.example.submission1intermediate.data.local

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.paging.*
import com.example.submission1intermediate.data.local.entity.StoryEntity
import com.example.submission1intermediate.data.local.room.RemoteKeysDao
import com.example.submission1intermediate.data.local.room.StoryDao
import com.example.submission1intermediate.data.local.room.StoryDatabase
import com.example.submission1intermediate.data.remote.ApiConfig
import com.example.submission1intermediate.data.remote.StoryRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthPreference (application: Application) {

    private val TOKEN_KEY = stringPreferencesKey("token_key")
    private val Context.dataStoreToken by preferencesDataStore("auth")
    private val dataStoreToken = application.dataStoreToken
    private val storyDao: StoryDao?
    private val remoteKeysDao: RemoteKeysDao?
    private val storyDb: StoryDatabase
   // private lateinit var apiService: ApiService


    init {
        storyDb = StoryDatabase.getInstance(application)
        storyDao = storyDb.storyDao()
        remoteKeysDao = storyDb.remoteKeysDao()

    }

    fun getAllStoriesWithPaging(token: String): LiveData<PagingData<StoryEntity>> {
        val apiService = ApiConfig.getApiService()

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = 8),
            remoteMediator = StoryRemoteMediator(storyDb, apiService , "Bearer $token"),
            pagingSourceFactory = {storyDb.storyDao().getAllStories()}
        ).liveData
    }

    fun getAuthToken(): Flow<String?> {
        return dataStoreToken.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun saveAuthToken(token: String){
        dataStoreToken.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }


}