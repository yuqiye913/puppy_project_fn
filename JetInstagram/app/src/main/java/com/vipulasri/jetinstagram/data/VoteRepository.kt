package com.vipulasri.jetinstagram.data

import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.network.LikeStatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VoteRepository {
    private val apiService = RetrofitInstance.api

    suspend fun likePost(postId: Long, token: String): Result<Unit> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.likePost(token, postId)
            }
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to like post: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlikePost(postId: Long, token: String): Result<Unit> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.unlikePost(token, postId)
            }
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to unlike post: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPostLikeStatus(postId: Long, token: String): Result<LikeStatusResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getPostLikeStatus(token, postId)
            }
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to get like status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeComment(commentId: Long, token: String): Result<Unit> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.likeComment(token, commentId)
            }
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to like comment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlikeComment(commentId: Long, token: String): Result<Unit> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.unlikeComment(token, commentId)
            }
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to unlike comment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCommentLikeStatus(commentId: Long, token: String): Result<LikeStatusResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getCommentLikeStatus(token, commentId)
            }
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("Failed to get comment like status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 