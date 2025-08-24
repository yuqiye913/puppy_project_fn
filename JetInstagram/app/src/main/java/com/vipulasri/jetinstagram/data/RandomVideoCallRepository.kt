package com.vipulasri.jetinstagram.data

import com.vipulasri.jetinstagram.network.ApiService
import com.vipulasri.jetinstagram.network.RandomVideoCallRequest
import com.vipulasri.jetinstagram.network.RandomVideoCallResponse
import com.vipulasri.jetinstagram.network.RandomVideoCallStatistics
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class RandomVideoCallRepository(
    private val apiService: ApiService
) {
    
    suspend fun requestRandomVideoCall(userId: Long): Result<RandomVideoCallResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthState.currentToken ?: throw Exception("No authentication token")
                
                val request = RandomVideoCallRequest(
                    userId = userId,
                    callType = "video",
                    enableVideo = true,
                    enableAudio = true,
                    videoQuality = "high",
                    audioQuality = "high",
                    preferredGender = "any",
                    preferredAgeRange = "any",
                    preferredLanguage = "en",
                    preferredLocation = "any",
                    isPriority = false,
                    queueType = "random"
                )
                
                println("🔵 RandomVideoCallRepository: Sending request for user $userId")
                val response = apiService.requestRandomVideoCall(token, request)
                
                if (response.isSuccessful) {
                    val dto = response.body() ?: RandomVideoCallResponse()
                    println("✅ RandomVideoCallRepository: Received DTO - requestId: ${dto.requestId}, status: ${dto.queueStatus}, position: ${dto.queuePosition}, totalUsers: ${dto.totalUsersInQueue}")
                    println("📊 RandomVideoCallRepository: Full DTO: $dto")
                    Result.success(dto)
                } else {
                    println("❌ RandomVideoCallRepository: Request failed with code ${response.code()}")
                    Result.failure(Exception("Failed to request random video call: ${response.code()}"))
                }
            } catch (e: Exception) {
                println("💥 RandomVideoCallRepository: Exception: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun checkQueueStatus(requestId: String): Result<RandomVideoCallResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthState.currentToken ?: throw Exception("No authentication token")
                val response = apiService.checkRandomVideoCallStatus(token, requestId)
                
                if (response.isSuccessful) {
                    val dto = response.body() ?: RandomVideoCallResponse()
                    println("🔄 RandomVideoCallRepository: Status check DTO - requestId: ${dto.requestId}, status: ${dto.queueStatus}, position: ${dto.queuePosition}")
                    println("📊 RandomVideoCallRepository: Full status DTO: $dto")
                    Result.success(dto)
                } else {
                    println("❌ RandomVideoCallRepository: Status check failed with code ${response.code()}")
                    Result.failure(Exception("Failed to check queue status: ${response.code()}"))
                }
            } catch (e: Exception) {
                println("💥 RandomVideoCallRepository: Status check exception: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun cancelRandomVideoCall(requestId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthState.currentToken ?: throw Exception("No authentication token")
                val response = apiService.cancelRandomVideoCall(token, requestId)
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to cancel random video call: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun acceptRandomVideoCall(requestId: String): Result<RandomVideoCallResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthState.currentToken ?: throw Exception("No authentication token")
                val response = apiService.acceptRandomVideoCall(token, requestId)
                
                if (response.isSuccessful) {
                    Result.success(response.body() ?: RandomVideoCallResponse())
                } else {
                    Result.failure(Exception("Failed to accept random video call: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun declineRandomVideoCall(requestId: String, reason: String? = null): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthState.currentToken ?: throw Exception("No authentication token")
                val response = apiService.declineRandomVideoCall(token, requestId, reason)
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to decline random video call: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun endRandomVideoCall(requestId: String): Result<RandomVideoCallResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthState.currentToken ?: throw Exception("No authentication token")
                val response = apiService.endRandomVideoCall(token, requestId)
                
                if (response.isSuccessful) {
                    Result.success(response.body() ?: RandomVideoCallResponse())
                } else {
                    Result.failure(Exception("Failed to end random video call: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getQueueStatistics(): Result<RandomVideoCallStatistics> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthState.currentToken ?: throw Exception("No authentication token")
                val response = apiService.getRandomVideoCallStatistics(token)
                
                if (response.isSuccessful) {
                    Result.success(response.body() ?: RandomVideoCallStatistics())
                } else {
                    Result.failure(Exception("Failed to get queue statistics: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getCurrentUserActiveRequest(): Result<RandomVideoCallResponse?> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthState.currentToken ?: throw Exception("No authentication token")
                val response = apiService.getCurrentUserActiveRequest(token)
                
                if (response.isSuccessful) {
                    Result.success(response.body())
                } else if (response.code() == 404) {
                    // No active request found
                    Result.success(null)
                } else {
                    Result.failure(Exception("Failed to get active request: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 