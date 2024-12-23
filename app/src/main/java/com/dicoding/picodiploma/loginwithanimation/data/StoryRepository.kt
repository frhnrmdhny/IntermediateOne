package com.dicoding.picodiploma.loginwithanimation.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.picodiploma.loginwithanimation.data.paging.Paging
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.remote.ApiService
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.AddStoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.remote.response.Story
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository private constructor(
    private val storiesApiService: ApiService,
    private val userPreference: UserPreference,
) {

    suspend fun logout() {
        userPreference.logout()
    }


    suspend fun uploadStories(description: RequestBody, photo: MultipartBody.Part): AddStoryResponse {
        return storiesApiService.uploadStory(photo, description)
    }

    suspend fun getStories(): List<ListStoryItem> {
        return try {
            val storyResponse = storiesApiService.getStories()
            storyResponse.listStory

        } catch (e: Exception) {
            Log.e("StoryRepository", "Error fetching stories", e)
            throw Exception("Error fetching stories: ${e.message}")
        }
    }

    fun getPagedStories(): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { Paging(storiesApiService) }
        ).flow
    }

    suspend fun getDetailStories(id: String): Story {
        return try {
            val response = storiesApiService.getStoryDetail(id)
            response.story
        } catch (e: Exception) {
            Log.e("StoryRepository", "Error fetching stories", e)
            throw Exception("Error fetching stories: ${e.message}")
        }
    }

    companion object {
        fun getInstance(
            storyApiService: ApiService,
            userPreference: UserPreference,
        ):
                StoryRepository = StoryRepository(storyApiService, userPreference)
    }
}