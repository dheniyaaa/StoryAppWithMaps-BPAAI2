package com.example.submission1intermediate.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.submission1intermediate.data.local.entity.RemoteKeys
import com.example.submission1intermediate.data.local.entity.StoryEntity
import com.example.submission1intermediate.data.local.room.StoryDatabase

@ExperimentalPagingApi
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService,
    private val token: String
): RemoteMediator<Int, StoryEntity>() {

    private companion object{
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {

        val page = when(loadType){
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prefKey = remoteKeys?.prevkey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prefKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val responseData = apiService.getAllStories(token, page, state.config.pageSize)
            val endPaginationReached = responseData.stories.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH){
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.storyDao().deleteAll()
                }

                val prevkey = if (page == 1) null else page - 1
                val nextKey = if (endPaginationReached) null else page + 1
                val keys = responseData.stories.map {
                    RemoteKeys(id = it.id, prevkey = prevkey, nextKey = nextKey)
                }

                //save remotekeys information to database
                database.remoteKeysDao().insertALl(keys)

                //Convert story class to storyEntity
                responseData.stories.forEach {  story ->
                    val storyEntity = StoryEntity(
                        story.id,
                        story.name,
                        story.description,
                        story.photoUrl,
                        story.lon,
                        story.lat,
                    )

                    database.storyDao().insertStory(storyEntity)
                }
            }

            return MediatorResult.Success(endOfPaginationReached = endPaginationReached)

        } catch (e: Exception){
            return MediatorResult.Error(e)
        }
        
        
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.lastOrNull{it.data.isNotEmpty()}?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.firstOrNull {it.data.isNotEmpty()}?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): RemoteKeys?{
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }
}