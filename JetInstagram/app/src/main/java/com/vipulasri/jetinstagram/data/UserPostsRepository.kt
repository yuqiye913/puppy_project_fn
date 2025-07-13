package com.vipulasri.jetinstagram.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.network.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserPostsRepository {
    private val _userPosts = mutableStateOf<List<Post>>(emptyList())
    val userPosts: State<List<Post>> = _userPosts
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error
    
    private val _hasMore = mutableStateOf(true)
    val hasMore: State<Boolean> = _hasMore
    
    private var currentCursor: String? = null
    
    suspend fun loadUserPosts(username: String, refresh: Boolean = false) {
        if (_isLoading.value) return
        
        try {
            _isLoading.value = true
            _error.value = null
            
            if (refresh) {
                _userPosts.value = emptyList()
                currentCursor = null
                _hasMore.value = true
            }
            
            val response = RetrofitInstance.api.getUserPosts(
                username = username,
                cursor = currentCursor,
                limit = 10
            )
            
            if (response.isSuccessful) {
                val cursorResponse = response.body()
                if (cursorResponse != null) {
                    val newPosts = cursorResponse.content.map { postResponse ->
                        Post(
                            id = postResponse.id.toInt(),
                            title = postResponse.postName, // Add title
                            text = postResponse.description,
                            image = postResponse.url,
                            user = User(
                                id = postResponse.userId, // Use the correct user ID from backend
                                name = postResponse.userName,
                                username = postResponse.userName,
                                image = "https://randomuser.me/api/portraits/men/1.jpg" // Default image
                            ),
                            isLiked = postResponse.upVote,
                            likesCount = postResponse.voteCount,
                            commentsCount = postResponse.commentCount,
                            timeStamp = System.currentTimeMillis(), // We'll need to parse duration
                            bestComment = null // We'll need to fetch comments separately if needed
                        )
                    }
                    
                    if (refresh) {
                        _userPosts.value = newPosts
                    } else {
                        _userPosts.value = _userPosts.value + newPosts
                    }
                    
                    currentCursor = cursorResponse.nextCursor
                    _hasMore.value = cursorResponse.nextCursor != null
                }
            } else {
                val errorMessage = ErrorHandler.parseErrorResponse(response)
                _error.value = errorMessage
            }
        } catch (e: Exception) {
            _error.value = "Error loading posts: ${e.localizedMessage}"
        } finally {
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun reset() {
        _userPosts.value = emptyList()
        _isLoading.value = false
        _error.value = null
        _hasMore.value = true
        currentCursor = null
    }
} 