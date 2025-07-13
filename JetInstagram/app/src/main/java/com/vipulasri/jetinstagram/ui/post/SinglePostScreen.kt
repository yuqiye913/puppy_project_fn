package com.vipulasri.jetinstagram.ui.post

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.vipulasri.jetinstagram.R
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.model.Comment
import com.vipulasri.jetinstagram.ui.components.*
import com.vipulasri.jetinstagram.ui.home.InlineClickableHashtagText
import com.vipulasri.jetinstagram.data.VoteRepository
import com.vipulasri.jetinstagram.data.CommentsRepository
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.vipulasri.jetinstagram.data.FollowRepository
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@ExperimentalFoundationApi
@Composable
fun SinglePostScreen(
    post: Post,
    onBackClick: () -> Unit,
    onUserAvatarClick: ((User) -> Unit)? = null,
    onHashtagClick: ((String) -> Unit)? = null,
    onLikeToggle: (Post) -> Unit = {},
    onLikeToggleApi: ((Long, Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Get follow status from FollowRepository
    val isFollowing = FollowRepository.getFollowStatus(post.user.id)
    val isLoadingFollowStatus = FollowRepository.isLoadingFollowStatus(post.user.id)
    val followError = FollowRepository.followError
    
    // Check follow status when component is first created
    LaunchedEffect(post.user.id) {
        FollowRepository.checkFollowStatus(post.user.id)
    }
    val commentsRepository = remember { CommentsRepository() }
    val comments by commentsRepository.comments.collectAsState()
    val isLoading by commentsRepository.isLoading.collectAsState()
    val error by commentsRepository.error.collectAsState()
    val hasMore by commentsRepository.hasMore.collectAsState()
    val replies by commentsRepository.replies.collectAsState()
    val loadingReplies by commentsRepository.loadingReplies.collectAsState()
    val errorReplies by commentsRepository.errorReplies.collectAsState()
    
    // State for current post data (with updated like status and counts)
    var currentPost by remember { mutableStateOf(post) }
    var isLoadingPost by remember { mutableStateOf(false) }
    
    // Load comments when the screen is first displayed
    LaunchedEffect(post.id) {
        commentsRepository.reset()
        commentsRepository.loadComments(post.id.toLong(), refresh = true)
    }
    
    // Fetch current post data to get updated like status and counts
    LaunchedEffect(post.id) {
        isLoadingPost = true
        try {
            val token = AuthState.currentToken
            println("DEBUG: Fetching post data - postId: ${post.id}")
            println("DEBUG: AuthState.currentToken is ${if (token != null) "present" else "null"}")
            
            if (token != null) {
                val response = com.vipulasri.jetinstagram.network.RetrofitInstance.api.getPostByIdAuthenticated("Bearer $token", post.id.toLong())
                if (response.isSuccessful) {
                    val postResponse = response.body()
                    if (postResponse != null) {
                        println("DEBUG: Post response - upVote: ${postResponse.upVote}, voteCount: ${postResponse.voteCount}")
                        currentPost = Post(
                            id = postResponse.id.toInt(),
                            title = postResponse.postName,
                            text = postResponse.description,
                            user = post.user, // Keep existing user data
                            likesCount = postResponse.voteCount,
                            commentsCount = postResponse.commentCount,
                            timeStamp = post.timeStamp, // Keep existing timestamp
                            isLiked = postResponse.upVote,
                            bestComment = null
                        )
                        println("DEBUG: Updated currentPost - isLiked: ${currentPost.isLiked}")
                    }
                } else {
                    println("DEBUG: Failed to fetch post data: ${response.code()}")
                    // Fallback to unauthenticated call if authenticated call fails
                    val fallbackResponse = com.vipulasri.jetinstagram.network.RetrofitInstance.api.getPostById(post.id.toLong())
                    if (fallbackResponse.isSuccessful) {
                        val postResponse = fallbackResponse.body()
                        if (postResponse != null) {
                            currentPost = Post(
                                id = postResponse.id.toInt(),
                                title = postResponse.postName,
                                text = postResponse.description,
                                user = post.user,
                                likesCount = postResponse.voteCount,
                                commentsCount = postResponse.commentCount,
                                timeStamp = post.timeStamp,
                                isLiked = false, // Default to false for unauthenticated requests
                                bestComment = null
                            )
                        }
                    }
                }
            } else {
                println("DEBUG: No token available for fetching post data")
                // Use unauthenticated call if no token
                val response = com.vipulasri.jetinstagram.network.RetrofitInstance.api.getPostById(post.id.toLong())
                if (response.isSuccessful) {
                    val postResponse = response.body()
                    if (postResponse != null) {
                        currentPost = Post(
                            id = postResponse.id.toInt(),
                            title = postResponse.postName,
                            text = postResponse.description,
                            user = post.user,
                            likesCount = postResponse.voteCount,
                            commentsCount = postResponse.commentCount,
                            timeStamp = post.timeStamp,
                            isLiked = false, // Default to false for unauthenticated requests
                            bestComment = null
                        )
                    }
                } else {
                    // Keep existing post data if API call fails
                    currentPost = post
                }
            }
        } catch (e: Exception) {
            println("DEBUG: Exception fetching post data: ${e.message}")
            // Handle error silently, keep existing post data
            currentPost = post
        } finally {
            isLoadingPost = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                title = {
                    Text(
                        text = "Post",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle share */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = { /* Handle more options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Black
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomCommentBar(
                onCommentSubmit = { commentText ->
                    val token = AuthState.currentToken
                    if (token != null) {
                        coroutineScope.launch {
                            commentsRepository.createComment(
                                postId = post.id.toLong(),
                                text = commentText,
                                token = token
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // Post Header
            item {
                PostHeader(
                    post = post, 
                    onUserAvatarClick = onUserAvatarClick,
                    isFollowing = isFollowing,
                    isLoadingFollowStatus = isLoadingFollowStatus,
                    onFollowToggle = {
                        FollowRepository.toggleFollow(post.user.id,
                            onSuccess = {
                                // Follow status updated successfully
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
            
            // Show follow error if any
            if (followError != null) {
                item {
                    Text(
                        text = followError!!,
                        style = MaterialTheme.typography.body2,
                        color = Color.Red,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            // Post Content
            item {
                PostContent(post, onHashtagClick)
            }
            
            // Post Actions
            item {
                PostActions(
                    post = currentPost, 
                    onLikeToggle = { clickedPost ->
                        // Update the current post state with the new like status
                        currentPost = clickedPost
                    }, 
                    onLikeToggleApi = onLikeToggleApi,
                    onUpdateCurrentPost = { updatedPost ->
                        currentPost = updatedPost
                    }
                )
            }
            

            
            // Comments Section Header
            item {
                CommentsSectionHeader(currentPost)
            }
            
            // Comments (threaded)
            items(comments) { comment ->
                CommentThread(
                    comment = comment,
                    postId = post.id.toLong(),
                    commentsRepository = commentsRepository,
                    replies = replies,
                    loadingReplies = loadingReplies,
                    errorReplies = errorReplies,
                    onUserAvatarClick = onUserAvatarClick,
                    onHashtagClick = onHashtagClick,
                    depth = 0,
                    maxDepth = Int.MAX_VALUE
                )
            }
            
            // Loading indicator for pagination
            if (isLoading && comments.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Load more comments when reaching the end
            if (hasMore && !isLoading) {
                item {
                    LaunchedEffect(Unit) {
                        commentsRepository.loadComments(post.id.toLong(), refresh = false)
                    }
                }
            }
            
            // Error message
            if (error != null) {
                item {
                    Text(
                        text = error!!,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            

        }
    }
}

@Composable
private fun PostHeader(
    post: Post, 
    onUserAvatarClick: ((User) -> Unit)? = null,
    isFollowing: Boolean = false,
    isLoadingFollowStatus: Boolean = false,
    onFollowToggle: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = Color.LightGray, shape = CircleShape)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true, radius = 20.dp)
                ) { 
                    onUserAvatarClick?.invoke(post.user) 
                }
        ) {
            Image(
                painter = rememberImagePainter(post.user.image),
                contentDescription = "Profile picture of ${post.user.username}",
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // User Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.user.username,
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
            Text(
                text = post.timeStamp.getTimeElapsedText(),
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
        }
        
        // Follow Button
        Button(
            onClick = onFollowToggle,
            enabled = !isLoadingFollowStatus,
            modifier = Modifier.height(32.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isFollowing) Color.Transparent else Color(0xFF6200EE),
                contentColor = if (isFollowing) Color.Black else Color.White
            ),
            border = if (isFollowing) {
                BorderStroke(1.dp, Color.LightGray)
            } else null,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoadingFollowStatus) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = if (isFollowing) Color.Black else Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    ImageBitmap.imageResource(id = R.drawable.addfriend),
                    contentDescription = if (isFollowing) "Unfollow" else "Add Friend",
                    modifier = Modifier.size(16.dp),
                    tint = if (isFollowing) Color.Black else Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isLoadingFollowStatus) "Loading..." else if (isFollowing) "Following" else "Add Friend",
                style = MaterialTheme.typography.caption.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun PostContent(post: Post, onHashtagClick: ((String) -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Post Title
        if (post.title.isNotEmpty()) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.h5.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }
        
        // Post Description
        if (post.text.isNotEmpty()) {
            InlineClickableHashtagText(
                text = post.text,
                style = MaterialTheme.typography.body1.copy(
                    lineHeight = 24.sp
                ),
                defaultColor = Color.Black,
                hashtagColor = Color(0xFF2196F3),
                onHashtagClick = { hashtag ->
                    onHashtagClick?.invoke(hashtag)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
        
        // Hashtags Section
        if (post.hashtags.isNotEmpty()) {
            HashtagsSection(post.hashtags, onHashtagClick)
        }
    }
}

@Composable
private fun PostActions(
    post: Post, 
    onLikeToggle: (Post) -> Unit, 
    onLikeToggleApi: ((Long, Boolean) -> Unit)?,
    onUpdateCurrentPost: (Post) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like Button
        AnimLikeButton(
            post = post, 
            onLikeClick = { clickedPost ->
                // Update local state immediately for responsive UI
                onLikeToggle(clickedPost)
            }, 
            onLikeToggle = { postId, shouldLike ->
                coroutineScope.launch {
                    val token = AuthState.currentToken
                    println("DEBUG: Like button clicked - postId: $postId, shouldLike: $shouldLike")
                    println("DEBUG: AuthState.currentToken is ${if (token != null) "present" else "null"}")
                    println("DEBUG: AuthState.isLoggedIn: ${AuthState.isLoggedIn}")
                    
                    if (token != null) {
                        println("DEBUG: Making API call to ${if (shouldLike) "like" else "unlike"} post")
                        val result = if (shouldLike) {
                            VoteRepository.likePost(postId, "Bearer $token")
                        } else {
                            VoteRepository.unlikePost(postId, "Bearer $token")
                        }
                        result.fold(
                            onSuccess = {
                                println("DEBUG: API call succeeded")
                                // Update the currentPost state with the new like status and count
                                val newLikesCount = if (shouldLike) post.likesCount + 1 else post.likesCount - 1
                                val updatedPost = post.copy(
                                    isLiked = shouldLike,
                                    likesCount = newLikesCount
                                )
                                onUpdateCurrentPost(updatedPost)
                                println("DEBUG: Updated currentPost - isLiked: ${updatedPost.isLiked}, likesCount: ${updatedPost.likesCount}")
                            },
                            onFailure = { exception ->
                                println("DEBUG: API call failed: ${exception.message}")
                                // If API call fails, we could revert the local state here
                                // For now, we'll keep it simple and let the user retry
                            }
                        )
                    } else {
                        println("DEBUG: No token available, cannot make API call")
                        // Could show a toast or error message here
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Like Count
        Text(
            text = "${post.likesCount}",
            style = MaterialTheme.typography.caption,
            color = Color.Gray,
            modifier = Modifier.padding(end = 16.dp)
        )

        // Comment Button
        PostIconButton {
            Icon(
                ImageBitmap.imageResource(id = R.drawable.ic_outlined_comment),
                contentDescription = "Comment",
                tint = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Comment Count
        Text(
            text = "${post.commentsCount}",
            style = MaterialTheme.typography.caption,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Share Button
        PostIconButton {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Bookmark Button
        PostIconButton {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Bookmark",
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun PostStats(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "${post.likesCount} likes",
            style = MaterialTheme.typography.subtitle2.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "${post.commentsCount} comments",
            style = MaterialTheme.typography.caption,
            color = Color.Gray
        )
    }
}

@Composable
private fun CommentsSectionHeader(post: Post) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Comments",
            style = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "${post.commentsCount}",
            style = MaterialTheme.typography.caption,
            color = Color.Gray
        )
    }
    
    Divider(color = Color.LightGray, thickness = 0.5.dp)
}

@Composable
private fun CommentItem(
    comment: Comment,
    postId: Long,
    onUserAvatarClick: ((User) -> Unit)? = null,
    onHashtagClick: ((String) -> Unit)? = null,
    commentsRepository: CommentsRepository? = null,
    onReplyClick: (() -> Unit)? = null,
    isReplying: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Commenter Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = Color.LightGray, shape = CircleShape)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true, radius = 16.dp)
                ) { 
                    onUserAvatarClick?.invoke(comment.user) 
                }
        ) {
            Image(
                painter = rememberImagePainter(comment.user.image),
                contentDescription = "Profile picture of ${comment.user.username}",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        // Comment Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.user.username,
                    style = MaterialTheme.typography.subtitle2.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comment.timeStamp.getTimeElapsedText(),
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            InlineClickableHashtagText(
                text = comment.text,
                style = MaterialTheme.typography.body2,
                defaultColor = Color.Black,
                hashtagColor = Color(0xFF2196F3),
                onHashtagClick = { hashtag ->
                    onHashtagClick?.invoke(hashtag)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Like and Reply Stats Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                CommentLikeButton(
                    comment = comment,
                    onLikeClick = { clickedComment ->
                        // Update local state immediately for responsive UI
                        // This will be handled by the comments repository
                    },
                    onLikeToggle = { commentId, shouldLike ->
                        coroutineScope.launch {
                            val token = AuthState.currentToken
                            if (token != null) {
                                val result = if (shouldLike) {
                                    VoteRepository.likeComment(commentId, "Bearer $token")
                                } else {
                                    VoteRepository.unlikeComment(commentId, "Bearer $token")
                                }
                                result.fold(
                                    onSuccess = {
                                        // Update the comment state optimistically for immediate UI feedback
                                        commentsRepository?.updateCommentLikeStatus(commentId, shouldLike)
                                    },
                                    onFailure = { exception ->
                                        // Handle like/unlike failure
                                        println("DEBUG: Comment like API call failed: ${exception.message}")
                                    }
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${comment.likesCount}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Reply button (message icon)
                Icon(
                    painter = painterResource(id = com.vipulasri.jetinstagram.R.drawable.ic_outlined_comment),
                    contentDescription = "Reply",
                    tint = if (isReplying) Color(0xFF9C27B0) else Color.Gray, // Purple when replying
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false, radius = 16.dp)
                        ) {
                            onReplyClick?.invoke()
                        }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${comment.replyCount}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun AddCommentSection(onCommentSubmit: (String) -> Unit) {
    var commentText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color = Color.LightGray, shape = CircleShape)
                .clip(CircleShape)
        ) {
            Image(
                painter = rememberImagePainter("https://randomuser.me/api/portraits/men/1.jpg"),
                contentDescription = "Your profile picture",
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Comment Input
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            placeholder = { Text("Add a comment...") },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                placeholderColor = Color.Gray
            ),
            textStyle = MaterialTheme.typography.body2,
            enabled = !isSubmitting
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Post Button
        TextButton(
            onClick = {
                if (commentText.trim().isNotEmpty()) {
                    isSubmitting = true
                    onCommentSubmit(commentText.trim())
                    commentText = ""
                    isSubmitting = false
                }
            },
            enabled = commentText.trim().isNotEmpty() && !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = Color(0xFF2196F3),
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = "Post",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
private fun HashtagsSection(hashtags: List<String>, onHashtagClick: ((String) -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Subreddits:",
            style = MaterialTheme.typography.caption,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            hashtags.forEach { hashtag ->
                Card(
                    modifier = Modifier
                        .padding(end = 8.dp, bottom = 4.dp)
                        .clickable { onHashtagClick?.invoke("#$hashtag") },
                    backgroundColor = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp
                ) {
                    Text(
                        text = "#$hashtag",
                        style = MaterialTheme.typography.caption,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommentThread(
    comment: Comment,
    postId: Long,
    commentsRepository: CommentsRepository,
    replies: Map<Int, List<Comment>>,
    loadingReplies: Map<Int, Boolean>,
    errorReplies: Map<Int, String?>,
    onUserAvatarClick: ((User) -> Unit)? = null,
    onHashtagClick: ((String) -> Unit)? = null,
    depth: Int = 0,
    maxDepth: Int = Int.MAX_VALUE,
    breadcrumbPath: List<Comment> = emptyList()
) {
    val coroutineScope = rememberCoroutineScope()
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var justCreatedReply by remember { mutableStateOf(false) }
    val token = com.vipulasri.jetinstagram.ui.auth.AuthState.currentToken
    
    // Show breadcrumb if this is a nested comment
    if (breadcrumbPath.isNotEmpty()) {
        CommentBreadcrumb(
            breadcrumbPath = breadcrumbPath,
            currentComment = comment,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
    
    Column(modifier = Modifier.padding(start = (depth * 16).dp)) {
        CommentItem(
            comment = comment,
            postId = postId,
            onUserAvatarClick = onUserAvatarClick,
            onHashtagClick = onHashtagClick,
            commentsRepository = commentsRepository,
            onReplyClick = { showReplyInput = !showReplyInput },
            isReplying = showReplyInput
        )
        if (showReplyInput) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Write a reply...") },
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Cancel button
                TextButton(
                    onClick = { 
                        showReplyInput = false
                        replyText = ""
                    },
                    enabled = !isSubmitting
                ) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = {
                        if (replyText.trim().isNotEmpty() && token != null) {
                            isSubmitting = true
                            coroutineScope.launch {
                                try {
                                    commentsRepository.createComment(
                                        postId = postId, // Use the main post ID
                                        text = replyText.trim(),
                                        parentCommentId = comment.id.toLong(), // Use the comment ID as parent
                                        token = token
                                    )
                                    replyText = ""
                                    showReplyInput = false
                                    justCreatedReply = true
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        }
                    },
                    enabled = replyText.trim().isNotEmpty() && !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color(0xFF2196F3),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Post")
                    }
                }
            }
        }
        if ((comment.replyCount > 0 || justCreatedReply) && depth < maxDepth) {
            val isLoading = loadingReplies[comment.id] == true
            val repliesList = replies[comment.id]
            val error = errorReplies[comment.id]
            var expanded by remember { mutableStateOf(justCreatedReply) }
            
            // Auto-expand if we just created a reply
            LaunchedEffect(repliesList) {
                if (repliesList != null && repliesList.isNotEmpty()) {
                    expanded = true
                }
            }
            if (!expanded) {
                TextButton(
                    onClick = {
                        expanded = true
                        coroutineScope.launch {
                            commentsRepository.loadReplies(comment.id)
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("View replies (${comment.replyCount})")
                }
            }
            if (expanded) {
                // Collapse button
                TextButton(
                    onClick = { expanded = false },
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "Hide replies",
                        color = Color.Gray,
                        style = MaterialTheme.typography.caption
                    )
                }
                
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp).padding(8.dp)
                    )
                } else if (error != null) {
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.caption
                    )
                } else if (repliesList != null) {
                    Column(
                        modifier = Modifier.padding(start = minOf(depth * 12, 60).dp)
                    ) {
                        repliesList.forEach { reply ->
                            CommentThread(
                                comment = reply,
                                postId = postId,
                                commentsRepository = commentsRepository,
                                replies = replies,
                                loadingReplies = loadingReplies,
                                errorReplies = errorReplies,
                                onUserAvatarClick = onUserAvatarClick,
                                onHashtagClick = onHashtagClick,
                                depth = depth + 1,
                                maxDepth = maxDepth,
                                breadcrumbPath = breadcrumbPath + comment
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun BottomCommentBar(onCommentSubmit: (String) -> Unit) {
    var commentText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color = Color.LightGray, shape = CircleShape)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = rememberImagePainter("https://randomuser.me/api/portraits/men/1.jpg"),
                    contentDescription = "Your profile picture",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Comment Input
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
                    placeholderColor = Color.Gray
                ),
                textStyle = MaterialTheme.typography.body2,
                enabled = !isSubmitting,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Post Button
            TextButton(
                onClick = {
                    if (commentText.trim().isNotEmpty()) {
                        isSubmitting = true
                        onCommentSubmit(commentText.trim())
                        commentText = ""
                        isSubmitting = false
                    }
                },
                enabled = commentText.trim().isNotEmpty() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "Post",
                        color = Color(0xFF2196F3),
                        style = MaterialTheme.typography.body2.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentBreadcrumb(
    breadcrumbPath: List<Comment>,
    currentComment: Comment,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show breadcrumb path
        breadcrumbPath.forEachIndexed { index, comment ->
            if (index > 0) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = comment.user.username,
                style = MaterialTheme.typography.caption,
                color = Color(0xFF2196F3),
                modifier = Modifier.clickable {
                    // Could add navigation to parent comment here
                }
            )
        }
        
        // Show arrow to current comment
        if (breadcrumbPath.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = currentComment.user.username,
                style = MaterialTheme.typography.caption,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun Long.getTimeElapsedText(): String {
    val now = System.currentTimeMillis()
    val time = this
    return DateUtils.getRelativeTimeSpanString(
        time, now, 0L, DateUtils.FORMAT_ABBREV_TIME
    ).toString()
} 