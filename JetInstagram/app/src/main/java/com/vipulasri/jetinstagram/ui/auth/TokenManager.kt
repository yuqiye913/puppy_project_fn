package com.vipulasri.jetinstagram.ui.auth

import com.vipulasri.jetinstagram.network.RefreshTokenRequest
import com.vipulasri.jetinstagram.network.RetrofitInstance
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

object TokenManager {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isRefreshing = AtomicBoolean(false)
    
    private val _isTokenValid = MutableStateFlow(true)
    val isTokenValid: StateFlow<Boolean> = _isTokenValid.asStateFlow()
    
    private var refreshJob: Job? = null
    private var currentRefreshToken: String? = null
    private var currentUsername: String? = null
    
    /**
     * Initialize token manager with refresh token and username
     */
    fun initialize(refreshToken: String, username: String) {
        currentRefreshToken = refreshToken
        currentUsername = username
        startAutoRefresh()
    }
    
    /**
     * Start automatic token refresh
     */
    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = coroutineScope.launch {
            while (isActive) {
                try {
                    // Refresh token every 14 minutes (JWT expires in 15 minutes)
                    delay(14 * 60 * 1000L)
                    refreshTokenIfNeeded()
                } catch (e: Exception) {
                    // If refresh fails, mark token as invalid
                    _isTokenValid.value = false
                    break
                }
            }
        }
    }
    
    /**
     * Refresh token if needed
     */
    suspend fun refreshTokenIfNeeded(): Boolean {
        if (isRefreshing.get()) {
            // Wait for ongoing refresh to complete
            while (isRefreshing.get()) {
                delay(100)
            }
            return _isTokenValid.value
        }
        
        if (!isRefreshing.compareAndSet(false, true)) {
            return _isTokenValid.value
        }
        
        return try {
            val refreshToken = currentRefreshToken
            val username = currentUsername
            
            if (refreshToken == null || username == null) {
                _isTokenValid.value = false
                return false
            }
            
            val request = RefreshTokenRequest(refreshToken, username)
            val response = RetrofitInstance.api.refreshToken(request)
            
            if (response.isSuccessful) {
                val refreshResponse = response.body()
                if (refreshResponse != null) {
                    // Update AuthState with new token
                    AuthState.currentToken = refreshResponse.authenticationToken
                    currentRefreshToken = refreshResponse.refreshToken
                    _isTokenValid.value = true
                    true
                } else {
                    _isTokenValid.value = false
                    false
                }
            } else {
                _isTokenValid.value = false
                false
            }
        } catch (e: Exception) {
            _isTokenValid.value = false
            false
        } finally {
            isRefreshing.set(false)
        }
    }
    
    /**
     * Force refresh token immediately
     */
    suspend fun forceRefreshToken(): Boolean {
        return refreshTokenIfNeeded()
    }
    
    /**
     * Clear token manager state
     */
    fun clear() {
        refreshJob?.cancel()
        currentRefreshToken = null
        currentUsername = null
        _isTokenValid.value = false
        isRefreshing.set(false)
    }
    
    /**
     * Check if token is currently being refreshed
     */
    fun isRefreshing(): Boolean = isRefreshing.get()
} 