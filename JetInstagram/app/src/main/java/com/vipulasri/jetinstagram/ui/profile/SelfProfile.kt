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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
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
import com.vipulasri.jetinstagram.data.BlockRepository
import com.vipulasri.jetinstagram.ui.home.PostView

@ExperimentalFoundationApi
@Composable
fun SelfProfile(user: User, onBackClick: () -> Unit = {}, onPostClick: ((Post) -> Unit)? = null, onUserAvatarClick: ((User) -> Unit)? = null) {
    val userPosts by ProfileRepository.userPosts
    val isLoading by ProfileRepository.isLoading
    val error by ProfileRepository.error
    val hasMorePages by ProfileRepository.hasMorePages
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
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
                    IconButton(onClick = { /* Handle settings */ }) {
                        Icon(
                            ImageBitmap.imageResource(id = R.drawable.customized),
                            contentDescription = "Settings",
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
            ProfileHeader(user, false) { 
                // No follow toggle for self profile
            }
            ProfileStats(userPosts.size, followerCount.toInt(), followingCount.toInt(), isLoadingCounts)
            ProfileBio(user)
            ProfileActions(user, false, false) { 
                // No follow toggle for self profile
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
            
                                    ProfilePostsGrid(userPosts, user, listState, isLoading, hasMorePages, onPostClick, onUserAvatarClick)
        }
    }
}

@Composable
private fun ProfileHeader(user: User, isFollowing: Boolean, onFollowToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color = Color.LightGray, shape = CircleShape)
                .clip(CircleShape)
        ) {
            Image(
                painter = rememberImagePainter(user.image),
                contentDescription = "Profile picture of ${user.username}",
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // User Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
            Text(
                text = user.name,
                style = MaterialTheme.typography.body1,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ProfileStats(postsCount: Int, followerCount: Int, followingCount: Int, isLoadingCounts: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Posts", postsCount.toString())
        StatItem("Followers", if (isLoadingCounts) "..." else followerCount.toString())
        StatItem("Following", if (isLoadingCounts) "..." else followingCount.toString())
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
            ),
            color = Color.Black
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = Color.Gray
        )
    }
}

@Composable
private fun ProfileBio(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = user.name,
            style = MaterialTheme.typography.subtitle1.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "This is your profile",
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { /* Edit profile action */ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
        ) {
            Text(
                text = "Edit Profile",
                color = Color.White,
                style = MaterialTheme.typography.button
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfilePostsGrid(
    posts: List<Post>,
    user: User,
    listState: LazyListState,
    isLoading: Boolean,
    hasMorePages: Boolean,
    onPostClick: ((Post) -> Unit)? = null,
    onUserAvatarClick: ((User) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val postsState = remember { mutableStateListOf<Post>().apply { addAll(posts) } }

    LaunchedEffect(posts) {
        postsState.clear()
        postsState.addAll(posts)
    }

    if (postsState.isEmpty() && !isLoading) {
        EmptyPostsState(user)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items(postsState) { post ->
                PostView(
                    post = post,
                    onDoubleClick = { clickedPost ->
                        println("SelfProfile: PostView clicked: ${clickedPost.id}")
                        onPostClick?.invoke(clickedPost)
                    },
                    onLikeToggle = { /* Handle like toggle */ },
                    onLikeToggleApi = { postId, shouldLike ->
                        scope.launch {
                            ProfileRepository.votePost(postId, shouldLike)
                        }
                    },
                    onUserAvatarClick = { clickedUser ->
                        // Prevent self-navigation - don't navigate to own profile
                        // Only navigate to other users' profiles
                        if (clickedUser.id != user.id) {
                            onUserAvatarClick?.invoke(clickedUser)
                        }
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
            text = "When you share photos and videos, they'll appear here.",
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
            color = Color(0xFF2196F3),
            modifier = Modifier.size(24.dp)
        )
    }
}
