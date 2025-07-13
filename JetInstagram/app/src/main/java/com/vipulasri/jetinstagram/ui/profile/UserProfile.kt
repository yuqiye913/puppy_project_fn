package com.vipulasri.jetinstagram.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.vipulasri.jetinstagram.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.vipulasri.jetinstagram.data.PostsRepository
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.ui.auth.AuthState
import com.vipulasri.jetinstagram.network.BlockRequest
import com.vipulasri.jetinstagram.network.RetrofitInstance
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.vipulasri.jetinstagram.data.ProfileRepository
import com.vipulasri.jetinstagram.data.FollowRepository
import com.vipulasri.jetinstagram.ui.home.PostView

@ExperimentalFoundationApi
@Composable
fun UserProfile(user: User, onBackClick: () -> Unit = {}, onPostClick: ((Post) -> Unit)? = null) {
    val userPosts by ProfileRepository.userPosts
    val isLoading by ProfileRepository.isLoading
    val error by ProfileRepository.error
    val hasMorePages by ProfileRepository.hasMorePages
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Get follow status from FollowRepository
    val isFollowing = FollowRepository.getFollowStatus(user.id)
    val isLoadingFollowStatus = FollowRepository.isLoadingFollowStatus(user.id)
    val followError = FollowRepository.followError
    
    // Get follower and following counts from ProfileRepository
    val followerCount by ProfileRepository.followerCount
    val followingCount by ProfileRepository.followingCount
    val isLoadingCounts by ProfileRepository.isLoadingCounts
    
    // Load initial posts when component is first created
    LaunchedEffect(user.id) {
        ProfileRepository.loadUserPosts(user.id.toLong(), refresh = true)
    }
    
    // Load follower and following counts when component is first created
    LaunchedEffect(user.id) {
        ProfileRepository.loadFollowerAndFollowingCounts(user.id)
        // Check follow status when component is first created
        FollowRepository.checkFollowStatus(user.id)
    }
    
    // Handle pagination when user scrolls to the bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    val lastVisibleItem = visibleItems.last()
                    val totalItems = listState.layoutInfo.totalItemsCount
                    
                    if (lastVisibleItem.index >= totalItems - 3 && hasMorePages && !isLoading) {
                        ProfileRepository.loadUserPosts(user.id.toLong(), refresh = false)
                    }
                }
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
                        text = user.username,
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val token = AuthState.currentToken?.let { "Bearer $it" }
                                if (token == null) {
                                    Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                
                                val blockedUserId = user.id
                                val request = BlockRequest(
                                    blockedUserId = blockedUserId,
                                    reason = "User requested block"
                                )
                                
                                try {
                                    val response = RetrofitInstance.api.blockUser(token, request)
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "User blocked successfully!", Toast.LENGTH_SHORT).show()
                                        onBackClick() // Navigate back after blocking
                                    } else {
                                        Toast.makeText(context, "Failed to block user: ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            ImageBitmap.imageResource(id = R.drawable.block),
                            contentDescription = "Block user",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            ProfileHeader(user, isFollowing) { 
                FollowRepository.toggleFollow(user.id,
                    onSuccess = {
                        // Follow status updated successfully
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            ProfileStats(userPosts.size, followerCount, followingCount, isLoadingCounts)
            ProfileBio(user)
            ProfileActions(user, isFollowing, isLoadingFollowStatus) { 
                FollowRepository.toggleFollow(user.id,
                    onSuccess = {
                        // Follow status updated successfully
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            // Show follow error if any
            followError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.body2,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Show error if any
            error?.let { errorMessage ->
                ErrorMessage(errorMessage) {
                    ProfileRepository.clearError()
                    scope.launch {
                        ProfileRepository.loadUserPosts(user.id.toLong(), refresh = true)
                    }
                }
            }
            
            ProfilePostsGrid(userPosts, user, listState, isLoading, hasMorePages, onPostClick)
        }
    }
}

@Composable
private fun ProfileHeader(user: User, isFollowing: Boolean, onFollowToggle: () -> Unit) {
    // Note: ProfileHeader doesn't need loading state as it's just for display
    // The actual follow toggle is handled in ProfileActions
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture
        Surface(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            shape = CircleShape,
            color = Color.LightGray
        ) {
            Image(
                painter = rememberImagePainter(user.image),
                contentDescription = user.username,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // User Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.name,
                style = MaterialTheme.typography.body2,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ProfileStats(postsCount: Int, followerCount: Long, followingCount: Long, isLoadingCounts: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Posts", postsCount.toString())
        StatItem("Followers", if (isLoadingCounts) "..." else formatCount(followerCount))
        StatItem("Following", if (isLoadingCounts) "..." else formatCount(followingCount))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}

// Helper function to format large numbers (e.g., 1200 -> "1.2K")
private fun formatCount(count: Long): String {
    return when {
        count >= 1000000 -> "${count / 1000000}M"
        count >= 1000 -> "${count / 1000}K"
        else -> count.toString()
    }
}

@Composable
private fun ProfileBio(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = user.name,
            style = MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Passionate about sharing moments and connecting with others. 📸✨",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}

@Composable
private fun ProfileActions(user: User, isFollowing: Boolean, isLoadingFollowStatus: Boolean, onFollowToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Add Friend Button
        Button(
            onClick = onFollowToggle,
            enabled = !isLoadingFollowStatus,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isFollowing) Color.Transparent else Color(0xFF6200EE),
                contentColor = if (isFollowing) Color.Black else Color.White
            ),
            border = if (isFollowing) {
                androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
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
                    modifier = Modifier.size(20.dp),
                    tint = if (isFollowing) Color.Black else Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isLoadingFollowStatus) "Loading..." else if (isFollowing) "Following" else "Add Friend",
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
        
        // Messages Button
        OutlinedButton(
            onClick = { /* Handle message */ },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
        ) {
            Icon(
                ImageBitmap.imageResource(id = R.drawable.messages),
                contentDescription = "Messages",
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Messages",
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.Black
            )
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun ProfilePostsGrid(
    posts: List<Post>, 
    user: User, 
    listState: LazyListState,
    isLoading: Boolean,
    hasMorePages: Boolean,
    onPostClick: ((Post) -> Unit)? = null
) {
    if (posts.isEmpty() && !isLoading) {
        EmptyPostsState(user)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items(posts) { post ->
                PostView(
                    post = post,
                    onDoubleClick = { clickedPost ->
                        println("UserProfile: PostView clicked: ${clickedPost.id}")
                        onPostClick?.invoke(clickedPost)
                    },
                    onLikeToggle = { /* Handle like toggle */ },
                    onUserAvatarClick = { user ->
                        // This will be handled by MainScreen
                    },
                    onHashtagClick = { hashtag ->
                        // This will be handled by MainScreen
                    },
                    onCommentClick = { clickedPost ->
                        onPostClick?.invoke(clickedPost)
                    }
                )
            }
            
            // Show loading indicator at the bottom if loading more
            if (isLoading && hasMorePages) {
                item {
                    LoadingIndicator()
                }
            }
        }
    }
}



@Composable
private fun EmptyPostsState(user: User) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Posts Yet",
            style = MaterialTheme.typography.h6,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "When ${user.username} shares photos and videos, they'll appear here.",
            style = MaterialTheme.typography.body2,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.body2,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Text("Retry", color = Color.White)
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colors.primary
        )
    }
} 