package com.example.submission1intermediate.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.PagingData
import com.example.submission1intermediate.data.local.AuthPreference
import com.example.submission1intermediate.data.local.entity.StoryEntity
import com.example.submission1intermediate.data.remote.*
import com.example.submission1intermediate.vstate.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.logging.Logger

class StoryRepository(
    private val authPreference: AuthPreference,
    private val remoteDataSource: RemoteDataSource,
): StoryDataSource {


    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Result<RegisterResponse>> = flow{
        try {
            val userRegister = ApiConfig.getApiService().register(name, email, password)
            emit(Result.success(userRegister))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun login(email: String, password: String): Flow<Result<LoginResponse>> = flow {
        try {
            val userLogin = ApiConfig.getApiService().login(email, password)
            emit(Result.success(userLogin))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getAllLocationStory(token: String): LiveData<Resource<ApiResponse<StoryResponse>>> = liveData(Dispatchers.IO){
        val bearerToken = "Bearer $token"
        val stories = remoteDataSource.getAllStory(bearerToken)
        try {
            emit(Resource.success(stories))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Resource.error("$e", stories))
        }


    }

    override fun getAllStoriesWithPaging(token: String): LiveData<PagingData<StoryEntity>> = authPreference.getAllStoriesWithPaging(token)


    override suspend fun uploadStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? ,
        lon: RequestBody?
    ) = liveData(Dispatchers.IO){
        val userUpload = remoteDataSource.uploadStory(token, file, description, lat, lon)
        try {
            emit(Resource.success(userUpload))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Resource.error("$e", userUpload))
        }
    }

    override suspend fun saveAuthToken(token: String) {
        authPreference.saveAuthToken(token)
    }

    override fun getAuthToken(): Flow<String?> = authPreference.getAuthToken()




}