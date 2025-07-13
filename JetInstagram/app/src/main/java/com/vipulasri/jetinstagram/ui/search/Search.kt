package com.vipulasri.jetinstagram.ui.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.vipulasri.jetinstagram.data.SearchRepository
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.ui.components.horizontalPadding
import com.vipulasri.jetinstagram.ui.components.verticalPadding
import com.vipulasri.jetinstagram.ui.home.PostView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.vipulasri.jetinstagram.data.VoteRepository

@ExperimentalFoundationApi
@Composable
fun Search(
    initialQuery: String = "", 
    onHashtagClick: ((String) -> Unit)? = null,
    onUserAvatarClick: ((User) -> Unit)? = null,
    onPostClick: ((Post) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var matchingUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var matchingPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Trigger search when initialQuery changes
    LaunchedEffect(initialQuery) {
        println("Search: initialQuery changed to: '$initialQuery'")
        if (initialQuery.isNotEmpty()) {
            searchQuery = initialQuery
            println("Search: Updated searchQuery to: '$searchQuery'")
        }
    }

    LaunchedEffect(searchQuery) {
        println("Search: searchQuery changed to: '$searchQuery'")
        if (searchQuery.isNotEmpty()) {
            println("Search: Starting search for: '$searchQuery'")
            isLoading = true
            searchError = null
            matchingUsers = emptyList()
            matchingPosts = emptyList()
            try {
                // Launch both searches in parallel
                coroutineScope.launch {
                    SearchRepository.searchUsers(searchQuery).collect { result ->
                        result.fold(
                            onSuccess = { users -> matchingUsers = users },
                            onFailure = { error -> searchError = error.message }
                        )
                    }
                }
                coroutineScope.launch {
                    SearchRepository.searchPosts(searchQuery).collect { result ->
                        result.fold(
                            onSuccess = { posts -> matchingPosts = posts },
                            onFailure = { error -> searchError = error.message }
                        )
                    }
                }
            } finally {
                isLoading = false
            }
        } else {
            matchingUsers = emptyList()
            matchingPosts = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClearClick = { searchQuery = "" }
        )
        if (searchQuery.isEmpty()) {
            PopularTags(onTagClick = { searchQuery = it })
        } else {
            if (isLoading) {
                LoadingIndicator()
            } else if (searchError != null) {
                ErrorMessage(searchError!!)
            } else {
                if (matchingUsers.isNotEmpty()) {
                    UserHorizontalBar(users = matchingUsers, onUserAvatarClick = onUserAvatarClick)
                }
                if (matchingPosts.isNotEmpty()) {
                    PostVerticalList(
                        posts = matchingPosts, 
                        onHashtagClick = onHashtagClick,
                        onUserAvatarClick = onUserAvatarClick,
                        onPostClick = onPostClick,
                        onVote = { postId, isUpvote ->
                            // For search results, we could implement a similar vote repository
                            // For now, we'll just show a toast or handle it differently
                            coroutineScope.launch {
                                // TODO: Implement vote functionality for search results
                                // This would require a similar approach as promoted posts
                            }
                        }
                    )
                } else if (matchingUsers.isEmpty() && matchingPosts.isEmpty()) {
                    EmptySearchResults(searchQuery)
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text("Search posts and users...")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                // Handle search action
            }
        ),
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.LightGray,
            unfocusedBorderColor = Color.LightGray
        )
    )
}

@Composable
private fun PopularTags(onTagClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Popular Tags",
            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Display popular tags as inline clickable hashtags
        com.vipulasri.jetinstagram.ui.home.InlineClickableHashtagText(
            text = popularTags.joinToString(" ") { "#$it" },
            style = MaterialTheme.typography.body2,
            defaultColor = Color.Gray,
            hashtagColor = Color(0xFF2196F3),
            onHashtagClick = { hashtag ->
                onTagClick(hashtag)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UserHorizontalBar(users: List<User>, onUserAvatarClick: ((User) -> Unit)? = null) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        items(users) { user ->
            UserAvatar(user, onUserAvatarClick)
        }
    }
}

@Composable
private fun UserAvatar(user: User, onUserAvatarClick: ((User) -> Unit)? = null) {
    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = androidx.compose.material.ripple.rememberRipple(bounded = true, radius = 28.dp)
                ) { 
                    onUserAvatarClick?.invoke(user) 
                },
            shape = CircleShape,
            color = Color.LightGray
        ) {
            Image(
                painter = rememberImagePainter(user.image),
                contentDescription = "Profile picture of ${user.username}",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.username,
            style = MaterialTheme.typography.caption,
            maxLines = 1
        )
    }
}

@ExperimentalFoundationApi
@Composable
private fun PostVerticalList(
    posts: List<Post>, 
    onHashtagClick: ((String) -> Unit)? = null,
    onUserAvatarClick: ((User) -> Unit)? = null,
    onPostClick: ((Post) -> Unit)? = null,
    onVote: ((Long, Boolean) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val postsState = remember { mutableStateListOf<Post>().apply { addAll(posts) } }

    LaunchedEffect(posts) {
        postsState.clear()
        postsState.addAll(posts)
    }

    LazyColumn {
        items(postsState) { post ->
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
                        val token = com.vipulasri.jetinstagram.ui.auth.AuthState.currentToken
                        if (token != null) {
                            val result = if (shouldLike) {
                                VoteRepository.likePost(postId, "Bearer $token")
                            } else {
                                VoteRepository.unlikePost(postId, "Bearer $token")
                            }
                            result.fold(
                                onSuccess = {
                                    // Update the post state based on the action
                                    val index = postsState.indexOfFirst { it.id.toLong() == postId }
                                    if (index != -1) {
                                        val currentPost = postsState[index]
                                        val newLikedState = shouldLike
                                        val newLikesCount = if (newLikedState) currentPost.likesCount + 1 else currentPost.likesCount - 1
                                        postsState[index] = currentPost.copy(isLiked = newLikedState, likesCount = newLikesCount)
                                    }
                                },
                                onFailure = { /* Optionally handle error */ }
                            )
                        }
                    }
                },
                onCommentClick = { clickedPost ->
                    onPostClick?.invoke(clickedPost)
                }
            )
        }
    }
}

@Composable
private fun EmptySearchResults(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No results found for \"$query\"",
            style = MaterialTheme.typography.h6,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try searching with different keywords",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.Gray
        )
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Search failed",
            style = MaterialTheme.typography.h6,
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
    }
}

private val popularTags = listOf(
    "coffee",
    "workout",
    "nature",
    "coding",
    "music",
    "food",
    "travel",
    "books"
)

 