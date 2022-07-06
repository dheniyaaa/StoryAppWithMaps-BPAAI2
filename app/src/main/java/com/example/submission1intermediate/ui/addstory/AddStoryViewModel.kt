package com.example.submission1intermediate.ui.addstory

import androidx.lifecycle.ViewModel
import com.example.submission1intermediate.data.StoryRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val storyRepository: StoryRepository): ViewModel() {

    suspend fun uploadImage(token: String, file: MultipartBody.Part, description: RequestBody,  lat: RequestBody? ,
                            lon: RequestBody?) = storyRepository.uploadStory(token, file, description, lat, lon)

    fun getAuthToken(): Flow<String?> = storyRepository.getAuthToken()
}