package com.vipulasri.jetinstagram.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vipulasri.jetinstagram.data.PromotedPostsRepository
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.ui.home.PostView
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun PromotedPostsSection(
    onUserAvatarClick: ((User) -> Unit)? = null,
    onPostClick: ((Post) -> Unit)? = null,
    onHashtagClick: ((String) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val promotedPosts by PromotedPostsRepository.promotedPosts
    val isLoading by PromotedPostsRepository.isLoading
    val error by PromotedPostsRepository.error

    // Use a key that changes when we want to refresh
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        if (refreshKey == 0) {
            // First time loading, reset the repository
            PromotedPostsRepository.reset()
        }
        PromotedPostsRepository.clearError() // Clear any previous errors
        PromotedPostsRepository.loadPromotedPosts(10)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Promoted",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Promoted Posts",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface
            )
        }

        // Content
        when {
            isLoading && promotedPosts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Failed to load promoted posts",
                            color = MaterialTheme.colors.error
                        )
                        TextButton(
                            onClick = {
                                refreshKey++ // Trigger LaunchedEffect to reload
                                coroutineScope.launch {
                                    PromotedPostsRepository.refreshPromotedPosts()
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            promotedPosts.isNotEmpty() -> {
                // Display promoted posts using the same PostView component as search
                Column {
                    promotedPosts.forEach { post ->
                        PostView(
                            post = post,
                            onDoubleClick = { clickedPost ->
                                onPostClick?.invoke(clickedPost)
                            },
                            onLikeToggle = { /* Handle like toggle */ },
                            onUserAvatarClick = onUserAvatarClick,
                            onHashtagClick = onHashtagClick,
                            onLikeToggleApi = { postId, shouldLike ->
                                coroutineScope.launch {
                                    PromotedPostsRepository.votePost(postId, shouldLike)
                                }
                            },
                            onCommentClick = { clickedPost ->
                                onPostClick?.invoke(clickedPost)
                            }
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No promoted posts available",
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
} 