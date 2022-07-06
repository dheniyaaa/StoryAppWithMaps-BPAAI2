package com.example.submission1intermediate.data

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.example.submission1intermediate.data.local.entity.StoryEntity
import com.example.submission1intermediate.data.remote.*
import com.example.submission1intermediate.vstate.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface StoryDataSource {

    suspend fun register(name: String, email: String, password: String): Flow<Result<RegisterResponse>>

    suspend fun login (email: String, password: String): Flow<Result<LoginResponse>>

    suspend fun getAllLocationStory(token:String): LiveData<Resource<ApiResponse<StoryResponse>>>

    fun getAllStoriesWithPaging(token: String): LiveData<PagingData<StoryEntity>>

    suspend fun uploadStory(token: String, file: MultipartBody.Part, description: RequestBody, lat: RequestBody? = null, lon: RequestBody? = null): LiveData<Resource<ApiResponse<FileUploadResponse>>>

    suspend fun saveAuthToken(token: String)

    fun getAuthToken(): Flow<String?>
}