package com.vipulasri.jetinstagram.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.vipulasri.jetinstagram.R
import com.vipulasri.jetinstagram.model.currentUser
import com.vipulasri.jetinstagram.ui.HomeSection.Add
import com.vipulasri.jetinstagram.ui.HomeSection.Home
import com.vipulasri.jetinstagram.ui.HomeSection.Matching
import com.vipulasri.jetinstagram.ui.HomeSection.Profile
import com.vipulasri.jetinstagram.ui.HomeSection.Search
import com.vipulasri.jetinstagram.ui.auth.AuthState
import com.vipulasri.jetinstagram.ui.components.bottomBarHeight
import com.vipulasri.jetinstagram.ui.components.icon
import com.vipulasri.jetinstagram.ui.home.Home
import com.vipulasri.jetinstagram.ui.search.Search
import com.vipulasri.jetinstagram.ui.profile.UserProfile
import com.vipulasri.jetinstagram.ui.profile.MyProfile
import com.vipulasri.jetinstagram.ui.matching.Matching
import com.vipulasri.jetinstagram.ui.matching.LonelyMatch
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.ui.post.SinglePostScreen
import com.vipulasri.jetinstagram.data.SinglePostRepository

@ExperimentalFoundationApi
@Composable
fun MainScreen() {

    val coroutineScope = rememberCoroutineScope()
    val sectionState = remember { mutableStateOf(Home) }
    val isViewingProfile = remember { mutableStateOf(false) }
    val isViewingLonelyMatch = remember { mutableStateOf(false) }
    val isViewingMatchingInProgress = remember { mutableStateOf(false) }
    val isViewingSinglePost = remember { mutableStateOf(false) }
    val currentProfileUser = remember { mutableStateOf<User?>(null) }
    val currentPost = remember { mutableStateOf<com.vipulasri.jetinstagram.model.Post?>(null) }
    val searchQuery = remember { mutableStateOf("") }
    
    // Current user (self) - use authenticated user info if available
    val currentUser = remember {
        val username = AuthState.currentUsername ?: "johndoe"
        val name = AuthState.currentUsername?.let { "User $it" } ?: "John Doe"
        val userId = AuthState.currentUserId ?: 1L
        User(
            id = userId,
            name = name,
            username = username,
            image = "https://randomuser.me/api/portraits/men/1.jpg"
        )
    }

    val navItems = HomeSection.values()
      .toList()
  Scaffold(
      bottomBar = {
        BottomBar(
            items = navItems,
            currentSection = sectionState.value,
            onSectionSelected = { 
                sectionState.value = it
                // Refresh promoted posts when navigating to Home
                if (it == Home) {
                    coroutineScope.launch {
                        com.vipulasri.jetinstagram.data.PromotedPostsRepository.refreshPromotedPosts()
                    }
                }
                // Close profile view when navigating with bottom bar
                if (isViewingProfile.value) {
                    isViewingProfile.value = false
                    currentProfileUser.value = null
                }
                // Close lonely match view when navigating with bottom bar
                if (isViewingLonelyMatch.value) {
                    isViewingLonelyMatch.value = false
                }
                // Close single post view when navigating with bottom bar
                if (isViewingSinglePost.value) {
                    isViewingSinglePost.value = false
                    currentPost.value = null
                }
            },
        )
      }) { innerPadding ->
    val modifier = Modifier.padding(innerPadding)
    Crossfade(
        modifier = modifier,
        targetState = when {
            isViewingSinglePost.value -> "singlePost"
            isViewingProfile.value -> "profile"
            isViewingLonelyMatch.value -> "lonelyMatch"
            isViewingMatchingInProgress.value -> "matchingInProgress"
            else -> sectionState.value.toString()
        }.also { state ->
            println("MainScreen: Current state determined as: $state")
            println("MainScreen: State values - isViewingProfile: ${isViewingProfile.value}, isViewingLonelyMatch: ${isViewingLonelyMatch.value}, isViewingMatchingInProgress: ${isViewingMatchingInProgress.value}, isViewingSinglePost: ${isViewingSinglePost.value}, currentPost: ${currentPost.value?.id}")
        })
    { currentState ->
        when (currentState) {
            "profile" -> {
                val user = currentProfileUser.value ?: currentUser
                if (user.username == currentUser.username) {
                    MyProfile(
                        user = user,
                        onBackClick = { 
                            isViewingProfile.value = false
                            currentProfileUser.value = null
                        },
                        onPostClick = { post ->
                            println("MainScreen: MyProfile post clicked: ${post.id}")
                            println("MainScreen: Setting currentPost to: ${post.id}")
                            currentPost.value = post
                            println("MainScreen: Setting isViewingSinglePost to true")
                            isViewingSinglePost.value = true
                            println("MainScreen: Current state after click - isViewingSinglePost: ${isViewingSinglePost.value}, currentPost: ${currentPost.value?.id}")
                        }
                    )
                } else {
                    UserProfile(
                        user = user,
                        onBackClick = { 
                            isViewingProfile.value = false
                            currentProfileUser.value = null
                        },
                        onPostClick = { post ->
                            println("MainScreen: UserProfile post clicked: ${post.id}")
                            println("MainScreen: Setting currentPost to: ${post.id}")
                            currentPost.value = post
                            println("MainScreen: Setting isViewingSinglePost to true")
                            isViewingSinglePost.value = true
                            println("MainScreen: Current state after click - isViewingSinglePost: ${isViewingSinglePost.value}, currentPost: ${currentPost.value?.id}")
                        }
                    )
                }
            }
            "lonelyMatch" -> LonelyMatch(
                onBackClick = { 
                    isViewingLonelyMatch.value = false
                },
                onStartMatchingNow = {
                    isViewingLonelyMatch.value = false
                    isViewingMatchingInProgress.value = true
                }
            )
            "matchingInProgress" -> com.vipulasri.jetinstagram.ui.matching.MatchingInProgress(
                onStopMatching = {
                    isViewingMatchingInProgress.value = false
                    isViewingLonelyMatch.value = true
                }
            )
            "singlePost" -> {
                println("MainScreen: Entering singlePost case")
                val post = currentPost.value
                println("MainScreen: currentPost.value = ${post?.id}")
                println("MainScreen: currentPost.value details = ${post?.title}, ${post?.text?.take(50)}")
                
                // Load post from repository if we have a post ID but no post data
                if (post != null) {
                    LaunchedEffect(post.id) {
                        SinglePostRepository.loadPostById(post.id.toLong())
                    }
                    
                    val loadedPost by SinglePostRepository.currentPost
                    val isLoading by SinglePostRepository.isLoading
                    val error by SinglePostRepository.error
                    
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        error != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Error: $error")
                                    Button(
                                        onClick = {
                                            SinglePostRepository.clearError()
                                            coroutineScope.launch {
                                                SinglePostRepository.loadPostById(post.id.toLong())
                                            }
                                        }
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        loadedPost != null -> {
                            println("MainScreen: Rendering SinglePostScreen with loaded post ID: ${loadedPost!!.id}")
                            SinglePostScreen(
                                post = loadedPost!!,
                                onBackClick = {
                                    println("MainScreen: SinglePostScreen back button clicked")
                                    isViewingSinglePost.value = false
                                    currentPost.value = null
                                    SinglePostRepository.reset()
                                },
                                onUserAvatarClick = { user ->
                                    // Check if clicking on own avatar or someone else's
                                    val isOwnProfile = user.username == currentUser.username
                                    currentProfileUser.value = user
                                    isViewingProfile.value = true
                                    isViewingSinglePost.value = false
                                    currentPost.value = null
                                    SinglePostRepository.reset()
                                },
                                onHashtagClick = { hashtag ->
                                    // Strip the # symbol for search since backend stores subreddits without #
                                    searchQuery.value = hashtag.removePrefix("#")
                                    sectionState.value = Search
                                    isViewingSinglePost.value = false
                                    currentPost.value = null
                                    SinglePostRepository.reset()
                                }
                            )
                        }
                        else -> {
                            // Fallback if no post is available
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Post not found")
                            }
                        }
                    }
                } else {
                    // Fallback if no post is available
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Post not found")
                    }
                }
            }
            else -> {
                when (sectionState.value) {
                    Home -> Home(
                        onUserAvatarClick = { user ->
                            // Check if clicking on own avatar or someone else's
                            val isOwnProfile = user.username == currentUser.username
                            currentProfileUser.value = user
                            isViewingProfile.value = true
                            // You could add a toast or other feedback here if needed
                        },
                        onHashtagClick = { hashtag ->
                            // Strip the # symbol for search since backend stores subreddits without #
                            searchQuery.value = hashtag.removePrefix("#")
                            sectionState.value = Search
                        },
                        onPostClick = { post ->
                            currentPost.value = post
                            isViewingSinglePost.value = true
                        }
                    )
                    Matching -> Matching(
                        onLonelyMatchClick = {
                            isViewingLonelyMatch.value = true
                        }
                    )
                    Add -> com.vipulasri.jetinstagram.ui.upload.Upload()
                    Search -> Search(
                        initialQuery = searchQuery.value,
                        onHashtagClick = { hashtag ->
                            // Strip the # symbol for search since backend stores subreddits without #
                            println("MainScreen: Hashtag clicked: '$hashtag'")
                            searchQuery.value = hashtag.removePrefix("#")
                            println("MainScreen: Updated searchQuery to: '${searchQuery.value}'")
                        },
                        onUserAvatarClick = { user ->
                            // Check if clicking on own avatar or someone else's
                            val isOwnProfile = user.username == currentUser.username
                            currentProfileUser.value = user
                            isViewingProfile.value = true
                        },
                        onPostClick = { post ->
                            currentPost.value = post
                            isViewingSinglePost.value = true
                        }
                    )
                    Profile -> {
                        currentProfileUser.value = currentUser
                        isViewingProfile.value = true
                    }
                }
            }
        }
    }
  }
}

@Composable
private fun Content(title: String) {
  Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
  ) {
    Text(
        text = title,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h5
    )
  }
}

@Composable
private fun BottomBar(
  items: List<HomeSection>,
  currentSection: HomeSection,
  onSectionSelected: (HomeSection) -> Unit,
) {
  BottomNavigation(
      modifier = Modifier.height(bottomBarHeight),
      backgroundColor = MaterialTheme.colors.background,
      contentColor = contentColorFor(MaterialTheme.colors.background)
  ) {
    items.forEach { section ->

      val selected = section == currentSection

      val iconRes = if (selected) section.selectedIcon else section.icon

      BottomNavigationItem(
          icon = {

            if (section == Profile) {
              BottomBarProfile(selected)
            } else {
              Icon(
                  ImageBitmap.imageResource(id = iconRes),
                  modifier = Modifier.icon(),
                  contentDescription = ""
              )
            }

          },
          selected = selected,
          onClick = { onSectionSelected(section) },
          alwaysShowLabel = false
      )
    }
  }
}

@Composable
private fun BottomBarProfile(isSelected: Boolean) {
  val shape = CircleShape

  val borderModifier = if (isSelected) {
    Modifier
        .border(
            color = Color.LightGray,
            width = 1.dp,
            shape = shape
        )
  } else Modifier

  val padding = if (isSelected) 3.dp else 0.dp

  Box(
      modifier = borderModifier
  ) {
    Box(
        modifier = Modifier.icon()
            .padding(padding)
            .background(color = Color.LightGray, shape = shape)
            .clip(shape)
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(id = R.drawable.me),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
  }

}

private enum class HomeSection(
  val icon: Int,
  val selectedIcon: Int
) {
  Home(R.drawable.ic_outlined_home, R.drawable.ic_filled_home),
  Matching(R.drawable.connection, R.drawable.connection),
  Add(R.drawable.upload, R.drawable.upload),
  Search(R.drawable.ic_outlined_search, R.drawable.ic_outlined_search),
  Profile(R.drawable.me, R.drawable.me)
}

