package com.vipulasri.jetinstagram.data

import com.vipulasri.jetinstagram.network.LoginRequest
import com.vipulasri.jetinstagram.network.RefreshTokenRequest
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.network.TokenUtils
import com.vipulasri.jetinstagram.ui.auth.AuthState
import com.vipulasri.jetinstagram.ui.auth.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthRepository {
    
    /**
     * Login with automatic token refresh handling
     */
    suspend fun login(username: String, password: String): Result<com.vipulasri.jetinstagram.network.LoginResponse> {
        return withContext(Dispatchers.IO) {
            TokenUtils.executeWithTokenRefresh {
                val request = LoginRequest(username, password)
                val response = RetrofitInstance.api.login(request)
                
                if (response.isSuccessful) {
                    response.body() ?: throw Exception("Empty response body")
                } else {
                    throw Exception("Login failed: ${response.code()}")
                }
            }
        }
    }
    
    /**
     * Refresh token manually
     */
    suspend fun refreshToken(): Result<com.vipulasri.jetinstagram.network.RefreshTokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = AuthState.currentRefreshToken
                val username = AuthState.currentUsername
                
                if (refreshToken == null || username == null) {
                    throw Exception("No refresh token or username available")
                }
                
                val request = RefreshTokenRequest(refreshToken, username)
                val response = RetrofitInstance.api.refreshToken(request)
                
                if (response.isSuccessful) {
                    val refreshResponse = response.body()
                    if (refreshResponse != null) {
                        // Update AuthState with new token
                        AuthState.updateToken(refreshResponse.authenticationToken)
                        AuthState.currentRefreshToken = refreshResponse.refreshToken
                        Result.success(refreshResponse)
                    } else {
                        Result.failure(Exception("Empty refresh response"))
                    }
                } else {
                    Result.failure(Exception("Token refresh failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Check if token is valid
     */
    fun isTokenValid(): Boolean {
        return AuthState.currentToken != null && AuthState.isLoggedIn
    }
    
    /**
     * Force refresh token
     */
    suspend fun forceRefreshToken(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val success = TokenManager.forceRefreshToken()
                Result.success(success)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 