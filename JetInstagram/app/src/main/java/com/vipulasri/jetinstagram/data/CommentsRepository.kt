package com.vipulasri.jetinstagram.data

import com.vipulasri.jetinstagram.model.Comment
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.network.ApiService
import com.vipulasri.jetinstagram.network.CursorPageResponse
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.network.CommentResponse
import com.vipulasri.jetinstagram.network.CreateCommentRequest
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update

class CommentsRepository {
    private val apiService: ApiService = RetrofitInstance.api
    
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    
    private var currentCursor: String? = null
    
    suspend fun loadComments(postId: Long, refresh: Boolean = false) {
        try {
            _isLoading.value = true
            _error.value = null
            
            if (refresh) {
                currentCursor = null
                _comments.value = emptyList()
            }
            
            val token = AuthState.currentToken
            val response = apiService.getCommentsForPost(
                token = token?.let { "Bearer $it" },
                postId = postId,
                cursor = currentCursor,
                limit = 20
            )
            
            if (response.isSuccessful) {
                val pageResponse = response.body()
                if (pageResponse != null) {
                    val newComments = pageResponse.content.map { it.toComment() }
                    
                    if (refresh) {
                        _comments.value = newComments
                    } else {
                        _comments.value = _comments.value + newComments
                    }
                    
                    currentCursor = pageResponse.nextCursor
                    _hasMore.value = pageResponse.hasMore
                }
            } else {
                _error.value = "Failed to load comments: ${response.code()}"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _error.value = "Error loading comments: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createComment(postId: Long, text: String, parentCommentId: Long? = null, token: String) {
        try {
            _isLoading.value = true
            _error.value = null
            
            val request = CreateCommentRequest(
                text = text,
                postId = postId,
                parentCommentId = parentCommentId
            )
            
            val response = apiService.createComment("Bearer $token", request)
            
            if (response.isSuccessful) {
                if (parentCommentId != null) {
                    // If this is a reply, update the reply count optimistically and load replies
                    _comments.update { comments ->
                        comments.map { comment ->
                            if (comment.id.toLong() == parentCommentId) {
                                comment.copy(replyCount = comment.replyCount + 1)
                            } else {
                                comment
                            }
                        }
                    }
                    loadReplies(parentCommentId.toInt())
                } else {
                    // If this is a top-level comment, refresh all comments
                    loadComments(postId, refresh = true)
                }
            } else {
                _error.value = "Failed to create comment: ${response.code()}"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _error.value = "Error creating comment: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun deleteComment(commentId: Long, postId: Long, token: String) {
        try {
            _isLoading.value = true
            _error.value = null
            
            val response = apiService.deleteComment("Bearer $token", commentId)
            
            if (response.isSuccessful) {
                // Refresh comments to reflect the deletion
                loadComments(postId, refresh = true)
            } else {
                _error.value = "Failed to delete comment: ${response.code()}"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _error.value = "Error deleting comment: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    fun reset() {
        _comments.value = emptyList()
        _isLoading.value = false
        _error.value = null
        _hasMore.value = true
        currentCursor = null
    }
    
    private fun CommentResponse.toComment(): Comment {
        return Comment(
            id = this.id.toInt(),
            text = this.text,
            user = User(
                id = 0L, // We don't have user ID in the response, using 0 as default
                name = this.userDisplayName ?: this.userName,
                username = this.userName,
                image = this.userProfilePicture ?: "https://randomuser.me/api/portraits/men/1.jpg"
            ),
            likesCount = this.voteCount,
            timeStamp = this.createdDate,
            replyCount = this.replyCount,
            parentCommentId = this.parentCommentId?.toInt(),
            isLiked = this.upVote
        )
    }

    // Replies state
    private val _replies = MutableStateFlow<Map<Int, List<Comment>>>(emptyMap())
    val replies: StateFlow<Map<Int, List<Comment>>> = _replies.asStateFlow()
    private val _loadingReplies = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val loadingReplies: StateFlow<Map<Int, Boolean>> = _loadingReplies.asStateFlow()
    private val _errorReplies = MutableStateFlow<Map<Int, String?>>(emptyMap())
    val errorReplies: StateFlow<Map<Int, String?>> = _errorReplies.asStateFlow()

    suspend fun loadReplies(commentId: Int) {
        try {
            _loadingReplies.update { it + (commentId to true) }
            _errorReplies.update { it + (commentId to null) }
            val response = apiService.getRepliesForComment(commentId.toLong())
            if (response.isSuccessful) {
                val pageResponse = response.body()
                if (pageResponse != null) {
                    val newReplies = pageResponse.content.map { it.toComment() }
                    _replies.update { it + (commentId to newReplies) }
                }
            } else {
                _errorReplies.update { it + (commentId to "Failed to load replies: ${response.code()}") }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _errorReplies.update { it + (commentId to "Error loading replies: ${e.message}") }
        } finally {
            _loadingReplies.update { it + (commentId to false) }
        }
    }
    
    fun updateCommentLikeStatus(commentId: Long, isLiked: Boolean) {
        _comments.update { comments ->
            comments.map { comment ->
                if (comment.id.toLong() == commentId) {
                    val newLikesCount = if (isLiked) comment.likesCount + 1 else comment.likesCount - 1
                    comment.copy(
                        isLiked = isLiked,
                        likesCount = newLikesCount
                    )
                } else {
                    comment
                }
            }
        }
        
        // Also update replies if the comment is in replies
        _replies.update { replies ->
            replies.mapValues { (_, commentList) ->
                commentList.map { comment ->
                    if (comment.id.toLong() == commentId) {
                        val newLikesCount = if (isLiked) comment.likesCount + 1 else comment.likesCount - 1
                        comment.copy(
                            isLiked = isLiked,
                            likesCount = newLikesCount
                        )
                    } else {
                        comment
                    }
                }
            }
        }
    }
} 