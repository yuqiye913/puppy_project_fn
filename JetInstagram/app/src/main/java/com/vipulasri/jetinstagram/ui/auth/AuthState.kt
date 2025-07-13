package com.vipulasri.jetinstagram.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vipulasri.jetinstagram.data.FollowRepository

object AuthState {
    var isLoggedIn by mutableStateOf(false)
    var currentToken by mutableStateOf<String?>(null)
    var currentUsername by mutableStateOf<String?>(null)
    var currentUserId by mutableStateOf<Long?>(null)
    var currentRefreshToken by mutableStateOf<String?>(null)
    
    fun login(token: String, username: String, userId: Long, refreshToken: String? = null) {
        currentToken = token
        currentUsername = username
        currentUserId = userId
        currentRefreshToken = refreshToken
        isLoggedIn = true
        
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
        
        // Clear token manager
        TokenManager.clear()
        
        // Clear follow statuses when user logs out
        FollowRepository.clearAllFollowStatuses()
    }
    
    /**
     * Update token (used by TokenManager)
     */
    fun updateToken(newToken: String) {
        currentToken = newToken
    }
} 