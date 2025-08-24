package com.vipulasri.jetinstagram.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vipulasri.jetinstagram.data.FollowRepository
import com.vipulasri.jetinstagram.data.BlockRepository

object AuthState {
    var isLoggedIn by mutableStateOf(false)
    var currentToken by mutableStateOf<String?>(null)
    var currentUsername by mutableStateOf<String?>(null)
    var currentUserId by mutableStateOf<Long?>(null)
    var currentRefreshToken by mutableStateOf<String?>(null)
    
    fun login(token: String, username: String, userId: Long, refreshToken: String? = null) {
        println("AuthState: login called with username: $username, userId: $userId")
        currentToken = token
        currentUsername = username
        currentUserId = userId
        currentRefreshToken = refreshToken
        isLoggedIn = true
        
        // Save to persistent storage
        TokenStorage.saveAuthData(token, refreshToken, username, userId)
        
        // Initialize token manager for auto-refresh
        if (refreshToken != null && username.isNotEmpty()) {
            TokenManager.initialize(refreshToken, username)
        }
    }
    
    fun logout() {
        currentToken = null
        currentUsername = null
        currentUserId = null
        currentRefreshToken = null
        isLoggedIn = false
        
        // Clear persistent storage
        TokenStorage.clearAuthData()
        
        // Clear token manager
        TokenManager.clear()
        
        // Clear follow statuses when user logs out
        FollowRepository.clearAllFollowStatuses()
        
        // Clear block data when user logs out
        BlockRepository.clearData()
    }
    
    /**
     * Update token (used by TokenManager)
     */
    fun updateToken(newToken: String) {
        currentToken = newToken
        TokenStorage.updateToken(newToken)
    }
    
    /**
     * Restore auth data from persistent storage
     */
    fun restoreAuthData() {
        println("AuthState: restoreAuthData called")
        if (TokenStorage.isLoggedIn()) {
            currentToken = TokenStorage.getAuthToken()
            currentUsername = TokenStorage.getUsername()
            currentUserId = TokenStorage.getUserId()
            currentRefreshToken = TokenStorage.getRefreshToken()
            isLoggedIn = true
            
            println("AuthState: restoreAuthData - currentUserId set to: $currentUserId")
            
            // Initialize token manager if we have refresh token
            val refreshToken = currentRefreshToken
            val username = currentUsername
            if (refreshToken != null && username != null) {
                TokenManager.initialize(refreshToken, username)
            }
        } else {
            println("AuthState: restoreAuthData - not logged in")
        }
    }
} 