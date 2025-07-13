package com.vipulasri.jetinstagram.data

import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.network.ApiService
import com.vipulasri.jetinstagram.network.ErrorHandler
import com.vipulasri.jetinstagram.network.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object SearchRepository {
    private val apiService = RetrofitInstance.api
    private val errorHandler = ErrorHandler

    fun searchUsers(query: String): Flow<Result<List<User>>> = flow {
        try {
            val response = apiService.searchUsers(query)
            if (response.isSuccessful) {
                val userResponses = response.body() ?: emptyList()
                val users = userResponses.map { userResponse ->
                    User(
                        id = userResponse.userId,
                        username = userResponse.username,
                        name = userResponse.username,
                        image = "https://via.placeholder.com/150"
                    )
                }
                emit(Result.success(users))
            } else {
                val error = Exception(errorHandler.parseErrorResponse(response))
                emit(Result.failure(error))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun searchPosts(query: String): Flow<Result<List<Post>>> = flow {
        try {
            // Use the new combined search endpoint - search by both subreddit name and post name
            val response = apiService.searchPosts(query = query)
            if (response.isSuccessful) {
                val postResponses = response.body() ?: emptyList()
                val posts = postResponses.map { postResponse ->
                    Post(
                        id = postResponse.id.toInt(),
                        title = postResponse.postName, // Post title
                        text = postResponse.description, // Post description with hashtags
                        user = User(
                            id = postResponse.userId, // Use the correct user ID from backend
                            username = postResponse.userName,
                            name = postResponse.userName,
                            image = "https://via.placeholder.com/150"
                        ),
                        likesCount = postResponse.voteCount,
                        commentsCount = postResponse.commentCount,
                        timeStamp = System.currentTimeMillis(),
                        isLiked = postResponse.upVote,
                        hashtags = postResponse.subredditNames ?: emptyList() // Convert subreddits to hashtags
                    )
                }
                emit(Result.success(posts))
            } else {
                val error = Exception(errorHandler.parseErrorResponse(response))
                emit(Result.failure(error))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 