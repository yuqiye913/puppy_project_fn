package com.vipulasri.jetinstagram.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.network.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SinglePostRepository {
    private val _currentPost = mutableStateOf<Post?>(null)
    val currentPost: State<Post?> = _currentPost
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error
    
    suspend fun loadPostById(postId: Long) {
        _isLoading.value = true
        _error.value = null
        
        try {
            withContext(Dispatchers.IO) {
                println("SinglePostRepository: Loading post with ID: $postId")
                val response = RetrofitInstance.api.getPostById(postId)
                
                if (response.isSuccessful) {
                    val postResponse = response.body()
                    if (postResponse != null) {
                        println("SinglePostRepository: Successfully loaded post: ${postResponse.id}")
                        val post = Post(
                            id = postResponse.id.toInt(),
                            title = postResponse.postName,
                            text = postResponse.description,
                            user = com.vipulasri.jetinstagram.model.User(
                                id = postResponse.userId,
                                name = postResponse.userName,
                                username = postResponse.userName,
                                image = "https://randomuser.me/api/portraits/men/${(postResponse.userId % 10) + 1}.jpg"
                            ),
                            likesCount = postResponse.voteCount,
                            commentsCount = postResponse.commentCount,
                            timeStamp = System.currentTimeMillis(),
                            isLiked = postResponse.upVote,
                            bestComment = null
                        )
                        _currentPost.value = post
                    } else {
                        _error.value = "Post not found"
                    }
                } else {
                    _error.value = ErrorHandler.parseErrorResponse(response)
                }
            }
        } catch (e: Exception) {
            _error.value = "Network error: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun reset() {
        _currentPost.value = null
        _isLoading.value = false
        _error.value = null
    }
} 