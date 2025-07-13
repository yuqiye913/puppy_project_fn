package com.vipulasri.jetinstagram.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vipulasri.jetinstagram.network.FollowRequest
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FollowRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // State for follow status
    var isFollowing by mutableStateOf<Map<Long, Boolean>>(emptyMap())
        private set
    
    var isLoadingFollowStatus by mutableStateOf<Map<Long, Boolean>>(emptyMap())
        private set
    
    var followError by mutableStateOf<String?>(null)
        private set
    
    /**
     * Check if current user is following a specific user
     */
    fun checkFollowStatus(followingId: Long) {
        if (isLoadingFollowStatus[followingId] == true) return
        
        coroutineScope.launch {
            try {
                setLoadingFollowStatus(followingId, true)
                clearError()
                
                val token = AuthState.currentToken
                if (token == null) {
                    setError("User not authenticated")
                    return@launch
                }
                
                val response = RetrofitInstance.api.isFollowingUser("Bearer $token", followingId)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val followStatus = response.body()
                        val isFollowingUser = followStatus?.following ?: false
                        updateFollowStatus(followingId, isFollowingUser)
                    } else {
                        setError("Failed to check follow status: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setError("Error checking follow status: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    setLoadingFollowStatus(followingId, false)
                }
            }
        }
    }
    
    /**
     * Follow a user
     */
    fun followUser(followingId: Long, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        coroutineScope.launch {
            try {
                setLoadingFollowStatus(followingId, true)
                clearError()
                
                val token = AuthState.currentToken
                if (token == null) {
                    setError("User not authenticated")
                    onError("User not authenticated")
                    return@launch
                }
                
                val request = FollowRequest(followingId)
                val response = RetrofitInstance.api.followUser("Bearer $token", request)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.success) {
                            updateFollowStatus(followingId, true)
                            onSuccess()
                        } else {
                            val errorMessage = body?.message ?: "Failed to follow user"
                            setError(errorMessage)
                            onError(errorMessage)
                        }
                    } else {
                        val errorMessage = "Failed to follow user: ${response.code()}"
                        setError(errorMessage)
                        onError(errorMessage)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = "Error following user: ${e.message}"
                    setError(errorMessage)
                    onError(errorMessage)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    setLoadingFollowStatus(followingId, false)
                }
            }
        }
    }
    
    /**
     * Unfollow a user
     */
    fun unfollowUser(followingId: Long, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        coroutineScope.launch {
            try {
                setLoadingFollowStatus(followingId, true)
                clearError()
                
                val token = AuthState.currentToken
                if (token == null) {
                    setError("User not authenticated")
                    onError("User not authenticated")
                    return@launch
                }
                
                val response = RetrofitInstance.api.unfollowUser("Bearer $token", followingId)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.success) {
                            updateFollowStatus(followingId, false)
                            onSuccess()
                        } else {
                            val errorMessage = body?.message ?: "Failed to unfollow user"
                            setError(errorMessage)
                            onError(errorMessage)
                        }
                    } else {
                        val errorMessage = "Failed to unfollow user: ${response.code()}"
                        setError(errorMessage)
                        onError(errorMessage)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = "Error unfollowing user: ${e.message}"
                    setError(errorMessage)
                    onError(errorMessage)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    setLoadingFollowStatus(followingId, false)
                }
            }
        }
    }
    
    /**
     * Toggle follow status (follow if not following, unfollow if following)
     */
    fun toggleFollow(followingId: Long, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val currentStatus = isFollowing[followingId] ?: false
        
        if (currentStatus) {
            unfollowUser(followingId, onSuccess, onError)
        } else {
            followUser(followingId, onSuccess, onError)
        }
    }
    
    /**
     * Get current follow status for a user
     */
    fun getFollowStatus(followingId: Long): Boolean {
        return isFollowing[followingId] ?: false
    }
    
    /**
     * Check if follow status is being loaded for a user
     */
    fun isLoadingFollowStatus(followingId: Long): Boolean {
        return isLoadingFollowStatus[followingId] ?: false
    }
    
    /**
     * Clear follow status for a user (useful when user logs out)
     */
    fun clearFollowStatus(followingId: Long) {
        isFollowing = isFollowing - followingId
        isLoadingFollowStatus = isLoadingFollowStatus - followingId
    }
    
    /**
     * Clear all follow statuses (useful when user logs out)
     */
    fun clearAllFollowStatuses() {
        isFollowing = emptyMap()
        isLoadingFollowStatus = emptyMap()
        followError = null
    }
    
    private fun updateFollowStatus(followingId: Long, isFollowingUser: Boolean) {
        isFollowing = isFollowing + (followingId to isFollowingUser)
    }
    
    private fun setLoadingFollowStatus(followingId: Long, loading: Boolean) {
        isLoadingFollowStatus = isLoadingFollowStatus + (followingId to loading)
    }
    
    private fun setError(error: String) {
        followError = error
    }
    
    private fun clearError() {
        followError = null
    }
} 