package com.vipulasri.jetinstagram.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.network.ErrorHandler
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ProfileRepository {
    private val _userPosts = mutableStateOf<List<Post>>(emptyList())
    val userPosts: State<List<Post>> = _userPosts
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error
    
    private val _hasMorePages = mutableStateOf(true)
    val hasMorePages: State<Boolean> = _hasMorePages
    
    // Follower and Following count states
    private val _followerCount = mutableStateOf(0L)
    val followerCount: State<Long> = _followerCount
    
    private val _followingCount = mutableStateOf(0L)
    val followingCount: State<Long> = _followingCount
    
    private val _isLoadingCounts = mutableStateOf(false)
    val isLoadingCounts: State<Boolean> = _isLoadingCounts
    
    private var currentCursor: String? = null
    private var currentUsername: String? = null
    
    suspend fun loadUserPosts(userId: Long, refresh: Boolean = false) {
        if (refresh) {
            currentCursor = null
            _userPosts.value = emptyList()
            _hasMorePages.value = true
            _error.value = null
        }
        
        if (!_hasMorePages.value || _isLoading.value) return
        
        _isLoading.value = true
        _error.value = null
        
        try {
            withContext(Dispatchers.IO) {
                val response = RetrofitInstance.api.getUserPostsById(
                    userId = userId,
                    cursor = currentCursor,
                    limit = 10
                )
                
                if (response.isSuccessful) {
                    val cursorResponse = response.body()
                    if (cursorResponse != null) {
                        val newPosts = cursorResponse.content.map { postResponse ->
                            println("ProfileRepository: Mapping post ${postResponse.id} with title: ${postResponse.postName}")
                            Post(
                                id = postResponse.id.toInt(),
                                title = postResponse.postName, // Add title
                                text = postResponse.description,
                                user = User(
                                    id = postResponse.userId, // Use the correct user ID from backend
                                    name = postResponse.userName,
                                    username = postResponse.userName,
                                    image = "https://randomuser.me/api/portraits/men/1.jpg"
                                ),
                                likesCount = postResponse.voteCount,
                                commentsCount = postResponse.commentCount,
                                timeStamp = System.currentTimeMillis(),
                                isLiked = postResponse.upVote,
                                bestComment = null // Remove mock comment
                            )
                        }
                        
                        if (refresh) {
                            _userPosts.value = newPosts
                            println("ProfileRepository: Refreshed posts, total count: ${newPosts.size}")
                        } else {
                            _userPosts.value = _userPosts.value + newPosts
                            println("ProfileRepository: Added posts, total count: ${_userPosts.value.size}")
                        }
                        
                        currentCursor = cursorResponse.nextCursor
                        _hasMorePages.value = cursorResponse.hasMore
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
    
    suspend fun loadFollowerAndFollowingCounts(userId: Long) {
        _isLoadingCounts.value = true
        _error.value = null
        
        try {
            withContext(Dispatchers.IO) {
                // Load follower count (no authentication required)
                val followerResponse = RetrofitInstance.api.getFollowerCount(userId)
                if (followerResponse.isSuccessful) {
                    followerResponse.body()?.let { followerCountResponse ->
                        _followerCount.value = followerCountResponse.followerCount
                    }
                } else {
                    _error.value = "Failed to load follower count: ${ErrorHandler.parseErrorResponse(followerResponse)}"
                }
                
                // Load following count (no authentication required)
                val followingResponse = RetrofitInstance.api.getFollowingCount(userId)
                if (followingResponse.isSuccessful) {
                    followingResponse.body()?.let { followingCountResponse ->
                        _followingCount.value = followingCountResponse.followingCount
                    }
                } else {
                    _error.value = "Failed to load following count: ${ErrorHandler.parseErrorResponse(followingResponse)}"
                }
            }
        } catch (e: Exception) {
            _error.value = "Network error: ${e.message}"
        } finally {
            _isLoadingCounts.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun reset() {
        _userPosts.value = emptyList()
        _isLoading.value = false
        _error.value = null
        _hasMorePages.value = true
        _followerCount.value = 0L
        _followingCount.value = 0L
        _isLoadingCounts.value = false
        currentCursor = null
        currentUsername = null
    }
} 