package com.vipulasri.jetinstagram.network

import com.vipulasri.jetinstagram.ui.auth.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TokenUtils {
    
    /**
     * Execute a function with automatic token refresh on 401 errors
     */
    suspend fun <T> executeWithTokenRefresh(
        apiCall: suspend () -> T
    ): Result<T> {
        return try {
            val result = apiCall()
            Result.success(result)
        } catch (e: Exception) {
            // Check if it's an authentication error
            if (isAuthenticationError(e)) {
                // Try to refresh token
                val refreshSuccess = withContext(Dispatchers.IO) {
                    TokenManager.refreshTokenIfNeeded()
                }
                
                if (refreshSuccess) {
                    // Retry the API call with new token
                    try {
                        val retryResult = apiCall()
                        Result.success(retryResult)
                    } catch (retryException: Exception) {
                        Result.failure(retryException)
                    }
                } else {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Check if an exception is an authentication error
     */
    private fun isAuthenticationError(exception: Exception): Boolean {
        return exception.message?.contains("401") == true ||
               exception.message?.contains("Unauthorized") == true ||
               exception.message?.contains("authentication") == true
    }
    
    /**
     * Get current authorization header
     */
    fun getAuthorizationHeader(): String? {
        return com.vipulasri.jetinstagram.ui.auth.AuthState.currentToken?.let { "Bearer $it" }
    }
} 