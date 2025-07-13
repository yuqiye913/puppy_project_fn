package com.vipulasri.jetinstagram.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.vipulasri.jetinstagram.data.ProfileRepository
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.ui.components.horizontalPadding
import com.vipulasri.jetinstagram.ui.components.verticalPadding
import com.vipulasri.jetinstagram.ui.home.PostView
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.launch
import com.vipulasri.jetinstagram.R
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@ExperimentalFoundationApi
@Composable
fun MyProfile(user: User, onBackClick: () -> Unit = {}, onPostClick: ((Post) -> Unit)? = null) {
    val userPosts by ProfileRepository.userPosts
    val isLoading by ProfileRepository.isLoading
    val error by ProfileRepository.error
    val hasMorePages by ProfileRepository.hasMorePages
    
    // Get follower and following counts from ProfileRepository
    val followerCount by ProfileRepository.followerCount
    val followingCount by ProfileRepository.followingCount
    val isLoadingCounts by ProfileRepository.isLoadingCounts
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
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
                    IconButton(onClick = { AuthState.logout() }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
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
            ProfileHeader(user)
            ProfileStats(userPosts.size, followerCount, followingCount, isLoadingCounts)
            ProfileBio(user)
            
            // Show error if any
            error?.let { errorMessage ->
                ErrorMessage(errorMessage) {
                    ProfileRepository.clearError()
                    coroutineScope.launch {
                        ProfileRepository.loadUserPosts(user.id.toLong(), refresh = true)
                    }
                }
            }
            
            ProfilePostsGrid(userPosts, user, listState, isLoading, hasMorePages, onPostClick)
        }
    }
}

@Composable
private fun ProfileHeader(user: User) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
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
            
            // Edit button
            IconButton(
                onClick = { /* Handle edit bio */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    ImageBitmap.imageResource(id = R.drawable.edit),
                    contentDescription = "Edit bio",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
            }
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
                        println("MyProfile: PostView clicked: ${clickedPost.id}")
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
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colors.primary
        )
    }
}