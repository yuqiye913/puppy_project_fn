package com.vipulasri.jetinstagram.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.GET
import retrofit2.http.Query

// Auth request/response data classes

data class SignupRequest(val username: String, val email: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(
    val authenticationToken: String,
    val refreshToken: String?,
    val expiresAt: Double?,   // Changed from Long? to Double?
    val username: String,
    val userId: Long
)

data class BlockRequest(
    val blockedUserId: Long,
    val reason: String? = null
)

data class BlockResponse(
    val blockId: Long,
    val blockerId: Long,
    val blockedUserId: Long,
    val blockerUsername: String,
    val blockedUserUsername: String,
    val reason: String?,
    val blockedAt: Long,
    val active: Boolean
)

// Follower and Following count data classes
data class FollowerCountResponse(
    val userId: Long,
    val username: String,
    val followerCount: Long
)

data class FollowingCountResponse(
    val userId: Long,
    val username: String,
    val followingCount: Long
)

data class SignupResponse(val message: String)

// Pagination response data classes
data class CursorPageResponse<T>(
    val content: List<T>,
    val nextCursor: String?,
    val hasMore: Boolean,
    val limit: Int
)

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean
)

data class PostResponse(
    val id: Long,
    val postName: String,
    val url: String?,
    val description: String,
    val userName: String,
    val userId: Long,  // Add user ID from backend
    val subredditName: String?,
    val subredditNames: List<String>?,
    val voteCount: Int,
    val commentCount: Int,
    val duration: String,
    val upVote: Boolean,
    val downVote: Boolean
)

// User search response
// (Assuming GetIntroDto fields: userId, username, bio)
data class UserSearchResponse(
    val userId: Long,
    val username: String,
    val bio: String?
)

// Error response data classes
data class ValidationErrorResponse(
    val timestamp: List<Int>?, // Backend sends timestamp as array [year,month,day,hour,minute,second,nanos]
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: Map<String, String>? = null
)



data class FollowResponse(
    val message: String,
    val success: Boolean,
    val followingId: Long
)

data class FollowStatusResponse(
    val userId: Long,
    val username: String?,
    val following: Boolean,
    val message: String
)

// Comment-related data classes
data class CommentResponse(
    val id: Long,
    val text: String,
    val userName: String,
    val userDisplayName: String?,
    val userProfilePicture: String?,
    val createdDate: Long,
    val voteCount: Int,
    val replyCount: Int,
    val parentCommentId: Long?,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val upVote: Boolean = false,
    val downVote: Boolean = false
)

data class CreateCommentRequest(
    val text: String,
    val postId: Long,
    val parentCommentId: Long? = null
)

data class LikeStatusResponse(
    val isLiked: Boolean,
    val likeCount: Int
)

interface ApiService {
    @POST("/api/posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body request: PostUploadRequest
    ): Response<Void>

    @POST("/api/follow/follow")
    suspend fun followUser(
        @Header("Authorization") token: String,
        @Body request: FollowRequest
    ): Response<FollowResponse>

    @DELETE("/api/follow/follow/{followingId}")
    suspend fun unfollowUser(
        @Header("Authorization") token: String,
        @Path("followingId") followingId: Long
    ): Response<FollowResponse>

    @POST("/api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/blocks")
    suspend fun blockUser(
        @Header("Authorization") token: String,
        @Body request: BlockRequest
    ): Response<BlockResponse>

    @GET("/api/posts")
    suspend fun getUserPosts(
        @Query("username") username: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<CursorPageResponse<PostResponse>>

    @GET("/api/posts")
    suspend fun getUserPostsById(
        @Query("userId") userId: Long,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<CursorPageResponse<PostResponse>>

    @GET("/api/posts/{id}")
    suspend fun getPostById(
        @Path("id") id: Long
    ): Response<PostResponse>
    
    @GET("/api/posts/{id}")
    suspend fun getPostByIdAuthenticated(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<PostResponse>

    @GET("/api/follow/followers/count/{userId}")
    suspend fun getFollowerCount(
        @Path("userId") userId: Long
    ): Response<FollowerCountResponse>

    @GET("/api/follow/following/count/{userId}")
    suspend fun getFollowingCount(
        @Path("userId") userId: Long
    ): Response<FollowingCountResponse>

    @GET("/api/follow/is-following/{followingId}")
    suspend fun isFollowingUser(
        @Header("Authorization") token: String,
        @Path("followingId") followingId: Long
    ): Response<FollowStatusResponse>

    @GET("/api/user/search")
    suspend fun searchUsers(
        @Query("username") username: String
    ): Response<List<UserSearchResponse>>

    @GET("/api/posts/search")
    suspend fun searchPosts(
        @Query("query") query: String
    ): Response<List<PostResponse>>
    
    @GET("/api/posts/promoted")
    suspend fun getPromotedPosts(
        @Header("Authorization") token: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<CursorPageResponse<PostResponse>>
    
    // Post like endpoints
    @POST("/api/votes/posts/{postId}/like")
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Response<Void>
    
    @DELETE("/api/votes/posts/{postId}/like")
    suspend fun unlikePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Response<Void>
    
    @GET("/api/votes/posts/{postId}/like")
    suspend fun getPostLikeStatus(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Response<LikeStatusResponse>
    
    // Comment like endpoints
    @POST("/api/votes/comments/{commentId}/like")
    suspend fun likeComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Long
    ): Response<Void>
    
    @DELETE("/api/votes/comments/{commentId}/like")
    suspend fun unlikeComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Long
    ): Response<Void>
    
    @GET("/api/votes/comments/{commentId}/like")
    suspend fun getCommentLikeStatus(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Long
    ): Response<LikeStatusResponse>
    
    // Comment endpoints
    @GET("/api/comments/post/{postId}")
    suspend fun getCommentsForPost(
        @Header("Authorization") token: String? = null,
        @Path("postId") postId: Long,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<CursorPageResponse<CommentResponse>>
    
    @POST("/api/comments")
    suspend fun createComment(
        @Header("Authorization") token: String,
        @Body request: CreateCommentRequest
    ): Response<Void>
    
    @DELETE("/api/comments/{commentId}")
    suspend fun deleteComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Long
    ): Response<Void>

    @GET("/api/comments/{commentId}/replies")
    suspend fun getRepliesForComment(
        @Path("commentId") commentId: Long,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<CursorPageResponse<CommentResponse>>
} 