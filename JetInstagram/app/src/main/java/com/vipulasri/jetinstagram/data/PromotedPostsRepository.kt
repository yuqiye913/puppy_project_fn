package com.vipulasri.jetinstagram.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.network.ApiService
import com.vipulasri.jetinstagram.network.CursorPageResponse
import com.vipulasri.jetinstagram.network.PostResponse
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.vipulasri.jetinstagram.data.VoteRepository

object PromotedPostsRepository {

    private val apiService = RetrofitInstance.api
    private val _promotedPosts = mutableStateOf<List<Post>>(emptyList())
    val promotedPosts: State<List<Post>> = _promotedPosts

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private var currentCursor: String? = null
    private var hasMore = true

    suspend fun loadPromotedPosts(limit: Int = 5) {
        if (_isLoading.value || !hasMore) {
            println("PromotedPostsRepository: Skipping load - isLoading: ${_isLoading.value}, hasMore: $hasMore")
            return
        }

        try {
            println("PromotedPostsRepository: Loading promoted posts with limit: $limit, cursor: $currentCursor")
            _isLoading.value = true
            _error.value = null

            val token = AuthState.currentToken
            println("PromotedPostsRepository: Token is ${if (token != null) "present" else "null"}")
            val response = withContext(Dispatchers.IO) {
                if (token != null) {
                    apiService.getPromotedPosts("Bearer $token", currentCursor, limit)
                } else {
                    apiService.getPromotedPosts(null, currentCursor, limit)
                }
            }

            if (response.isSuccessful) {
                val pageResponse = response.body()
                if (pageResponse != null) {
                    println("PromotedPostsRepository: Received ${pageResponse.content.size} posts, hasMore: ${pageResponse.hasMore}")
                    val newPosts = pageResponse.content.map { postResponse ->
                        convertToPost(postResponse)
                    }
                    
                    if (currentCursor == null) {
                        // First page
                        _promotedPosts.value = newPosts
                        println("PromotedPostsRepository: Set first page with ${newPosts.size} posts")
                    } else {
                        // Append to existing posts
                        _promotedPosts.value = _promotedPosts.value + newPosts
                        println("PromotedPostsRepository: Appended ${newPosts.size} posts to existing ${_promotedPosts.value.size - newPosts.size} posts")
                    }
                    
                    currentCursor = pageResponse.nextCursor
                    hasMore = pageResponse.hasMore
                }
            } else {
                println("PromotedPostsRepository: API call failed with code: ${response.code()}")
                _error.value = "Failed to load promoted posts: ${response.code()}"
            }
        } catch (e: Exception) {
            println("PromotedPostsRepository: Exception occurred: ${e.message}")
            _error.value = "Error loading promoted posts: ${e.message}"
        } finally {
            _isLoading.value = false
            println("PromotedPostsRepository: Loading completed, isLoading set to false")
        }
    }

    suspend fun refreshPromotedPosts(limit: Int = 5) {
        println("PromotedPostsRepository: Refreshing promoted posts with limit: $limit")
        currentCursor = null
        hasMore = true
        _promotedPosts.value = emptyList()
        _isLoading.value = false
        _error.value = null
        loadPromotedPosts(limit)
    }

    suspend fun votePost(postId: Long, shouldLike: Boolean) {
        val token = AuthState.currentToken
        if (token == null) {
            _error.value = "User not authenticated"
            return
        }

        try {
            val result = if (shouldLike) {
                VoteRepository.likePost(postId, "Bearer $token")
            } else {
                VoteRepository.unlikePost(postId, "Bearer $token")
            }

            result.fold(
                onSuccess = {
                    updatePostVoteState(postId, shouldLike)
                },
                onFailure = { exception ->
                    _error.value = "Failed to ${if (shouldLike) "like" else "unlike"}: ${exception.message}"
                }
            )
        } catch (e: Exception) {
            _error.value = "Error ${if (shouldLike) "liking" else "unliking"}: ${e.message}"
        }
    }

    private fun convertToPost(postResponse: PostResponse): Post {
        return Post(
            id = postResponse.id.toInt(),
            title = postResponse.postName, // Post title
            text = postResponse.description, // Post description with hashtags
            user = User(
                id = postResponse.userId, // Use the correct user ID, not post ID
                name = postResponse.userName,
                username = postResponse.userName,
                image = "https://randomuser.me/api/portraits/men/${(postResponse.userId % 10) + 1}.jpg"
            ),
            likesCount = postResponse.voteCount,
            commentsCount = postResponse.commentCount,
            timeStamp = System.currentTimeMillis(),
            bestComment = null, // Remove mock comment
            isLiked = postResponse.upVote,
            hashtags = postResponse.subredditNames ?: emptyList() // Convert subreddits to hashtags
        )
    }

    fun clearError() {
        _error.value = null
    }

    fun reset() {
        _promotedPosts.value = emptyList()
        _isLoading.value = false
        _error.value = null
        currentCursor = null
        hasMore = true
    }

    private fun updatePostVoteState(postId: Long, isLiked: Boolean) {
        val currentPosts = _promotedPosts.value.toMutableList()
        val postIndex = currentPosts.indexOfFirst { it.id.toLong() == postId }
        
        if (postIndex != -1) {
            val post = currentPosts[postIndex]
            val newLikesCount = if (isLiked) {
                post.likesCount + 1
            } else {
                post.likesCount - 1
            }
            
            currentPosts[postIndex] = post.copy(
                isLiked = isLiked,
                likesCount = newLikesCount
            )
            
            _promotedPosts.value = currentPosts
        }
    }
} 