package com.example.submission1intermediate.ui.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.submission1intermediate.data.StoryRepository
import com.example.submission1intermediate.data.remote.ApiResponse
import com.example.submission1intermediate.data.remote.StoryResponse
import com.example.submission1intermediate.vstate.Resource

//@ExperimentalPagingApi
class LocationViewModel(private val storyRepository: StoryRepository): ViewModel() {

    suspend fun getAllLocationStory(token: String): LiveData<Resource<ApiResponse<StoryResponse>>> = storyRepository.getAllLocationStory(token)
}