package com.vipulasri.jetinstagram.network

import com.vipulasri.jetinstagram.ui.auth.AuthState
import com.vipulasri.jetinstagram.ui.auth.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class TokenInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Add authorization header if token exists
        val request = if (AuthState.currentToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer ${AuthState.currentToken}")
                .build()
        } else {
            originalRequest
        }
        
        // Execute the request
        val response = chain.proceed(request)
        
        // If we get a 401 (Unauthorized), try to refresh the token
        if (response.code == 401 && AuthState.isLoggedIn) {
            response.close()
            
            return runBlocking {
                try {
                    // Try to refresh the token
                    val refreshSuccess = TokenManager.refreshTokenIfNeeded()
                    
                    if (refreshSuccess && AuthState.currentToken != null) {
                        // Retry the original request with the new token
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${AuthState.currentToken}")
                            .build()
                        chain.proceed(newRequest)
                    } else {
                        // Refresh failed, return the original 401 response
                        response
                    }
                } catch (e: Exception) {
                    // If refresh fails, return the original 401 response
                    response
                }
            }
        }
        
        return response
    }
} 