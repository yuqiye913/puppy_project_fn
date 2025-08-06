package com.vipulasri.jetinstagram.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.network.BlockResponse
import com.vipulasri.jetinstagram.network.BlockRequest
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.network.ErrorHandler
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BlockedUser(
    val blockId: Long,
    val userId: Long,
    val username: String,
    val displayName: String?,
    val profilePicture: String?,
    val reason: String?,
    val blockedAt: Long,
    val isActive: Boolean
)

object BlockRepository {
    private val _blockedUsers = mutableStateOf<List<BlockedUser>>(emptyList())
    val blockedUsers: State<List<BlockedUser>> = _blockedUsers
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error
    
    private val _hasMorePages = mutableStateOf(true)
    val hasMorePages: State<Boolean> = _hasMorePages
    
    private var currentPage = 0
    private val pageSize = 10
    
    /**
     * Load blocked users from the API
     */
    suspend fun loadBlockedUsers(refresh: Boolean = false) {
        if (_isLoading.value) return
        
        try {
            _isLoading.value = true
            _error.value = null
            
            if (refresh) {
                _blockedUsers.value = emptyList()
                currentPage = 0
                _hasMorePages.value = true
            }
            
            val token = AuthState.currentToken
            println("BlockRepository: Token available: ${token != null}")
            if (token == null) {
                _error.value = "User not authenticated"
                return
            }
            
            println("BlockRepository: Making API call to get blocked users...")
            val response = RetrofitInstance.api.getBlockedUsers(
                token = "Bearer $token",
                page = currentPage,
                size = pageSize
            )
            println("BlockRepository: API response code: ${response.code()}")
            println("BlockRepository: API response successful: ${response.isSuccessful}")
            
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val blockListResponse = response.body()
                    if (blockListResponse != null) {
                        val newBlockedUsers = blockListResponse.blockedUsers.map { blockResponse ->
                            BlockedUser(
                                blockId = blockResponse.blockId,
                                userId = blockResponse.blockedUserId,
                                username = blockResponse.blockedUserUsername,
                                displayName = blockResponse.blockedUserDisplayName,
                                profilePicture = blockResponse.blockedUserProfilePicture,
                                reason = blockResponse.reason,
                                blockedAt = blockResponse.blockedAt ?: System.currentTimeMillis(),
                                isActive = blockResponse.isActive
                            )
                        }
                        
                        if (refresh) {
                            _blockedUsers.value = newBlockedUsers
                        } else {
                            _blockedUsers.value = _blockedUsers.value + newBlockedUsers
                        }
                        
                        _hasMorePages.value = blockListResponse.hasMore ?: false
                        currentPage++
                    }
                } else {
                    val errorMessage = ErrorHandler.parseErrorResponse(response)
                    _error.value = errorMessage
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _error.value = "Error loading blocked users: ${e.message}"
            }
        } finally {
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Block a user
     */
    suspend fun blockUser(userId: Long, reason: String? = null, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        try {
            val token = AuthState.currentToken
            if (token == null) {
                onError("User not authenticated")
                return
            }
            
            val request = BlockRequest(blockedUserId = userId, reason = reason)
            val response = RetrofitInstance.api.blockUser("Bearer $token", request)
            
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorMessage = ErrorHandler.parseErrorResponse(response)
                    onError(errorMessage)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Error blocking user: ${e.message}")
            }
        }
    }
    
    /**
     * Unblock a user
     */
    suspend fun unblockUser(userId: Long, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        try {
            val token = AuthState.currentToken
            if (token == null) {
                onError("User not authenticated")
                return
            }
            
            val request = com.vipulasri.jetinstagram.network.UnblockRequest(blockedUserId = userId)
            val response = RetrofitInstance.api.unblockUser("Bearer $token", request)
            
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    // Remove the user from the blocked list
                    _blockedUsers.value = _blockedUsers.value.filter { it.userId != userId }
                    onSuccess()
                } else {
                    val errorMessage = ErrorHandler.parseErrorResponse(response)
                    onError(errorMessage)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Error unblocking user: ${e.message}")
            }
        }
    }
    
    /**
     * Check if a user is blocked
     */
    suspend fun hasBlockedUser(userId: Long): Boolean {
        return try {
            val token = AuthState.currentToken
            if (token == null) return false
            
            val response = RetrofitInstance.api.hasBlockedUser("Bearer $token", userId)
            response.isSuccessful && response.body() == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Clear all data (useful when user logs out)
     */
    fun clearData() {
        _blockedUsers.value = emptyList()
        _isLoading.value = false
        _error.value = null
        _hasMorePages.value = true
        currentPage = 0
    }
} 