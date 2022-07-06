package com.example.submission1intermediate.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import retrofit2.Response.error

class RemoteDataSource {


    suspend fun register(
        name: String,
        email: String,
        password: String
    ): ApiResponse<RegisterResponse>{
        val response = ApiConfig.getApiService().register(name, email, password)
        return try {
            ApiResponse.success(response)
        } catch (e: Exception){
            ApiResponse.error("$e", response )
        }
    }

    suspend fun login(email: String, password: String): ApiResponse<LoginResponse>{

        return try {
            val response = ApiConfig.getApiService().login(email, password)
            ApiResponse.success(response)
          //  if (response.error!!.equals(401)) == ApiResponse.error("invalid", response)
        } catch (e: Exception){
            val response = ApiConfig.getApiService().login(email, password)
            e.printStackTrace()
            ApiResponse.error("$e", response)

        }
    }

    suspend fun getAllStory(token:String): ApiResponse<StoryResponse>{
        val bearerToken = "Bearer $token"
        val response =ApiConfig.getApiService().getAllStories(token, size = 30, location = 1)

        return try {
            ApiResponse.success(response)

        } catch (e: Exception){
            ApiResponse.error("$e", response)
        }
    }

    suspend fun uploadStory(token: String, file: MultipartBody.Part , description: RequestBody, lat: RequestBody? = null, lon: RequestBody? = null): ApiResponse<FileUploadResponse>{

        val bearerToken = "Bearer $token"
        val response = ApiConfig.getApiService().uploadStory(bearerToken, file, description, lat, lon)
        return try {
            ApiResponse.success(response)

        } catch ( e: Exception){
            ApiResponse.error("$e", response)
        }
    }

}