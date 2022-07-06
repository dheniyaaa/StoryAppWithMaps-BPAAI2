package com.example.submission1intermediate

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.submission1intermediate.data.StoryRepository
import com.example.submission1intermediate.data.local.entity.StoryEntity
import kotlinx.coroutines.launch

class MainViewModel(private val storyRepository: StoryRepository): ViewModel() {

    fun saveAuthToken(token: String) {
        viewModelScope.launch { storyRepository.saveAuthToken(token) }
    }

    fun getAllStory(token: String): LiveData<PagingData<StoryEntity>> = storyRepository.getAllStoriesWithPaging(token).cachedIn(viewModelScope)
}