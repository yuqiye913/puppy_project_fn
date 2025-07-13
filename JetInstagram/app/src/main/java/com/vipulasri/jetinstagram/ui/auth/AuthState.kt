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
    
    fun login(token: String, username: String, userId: Long) {
        currentToken = token
        currentUsername = username
        currentUserId = userId
        isLoggedIn = true
    }
    
    fun logout() {
        currentToken = null
        currentUsername = null
        currentUserId = null
        isLoggedIn = false
        // Clear follow statuses when user logs out
        FollowRepository.clearAllFollowStatuses()
    }
} 