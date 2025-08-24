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
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.vipulasri.jetinstagram.R

import com.vipulasri.jetinstagram.ui.auth.AuthState
import com.vipulasri.jetinstagram.ui.components.bottomBarHeight
import com.vipulasri.jetinstagram.ui.components.icon
import com.vipulasri.jetinstagram.ui.home.Home
import com.vipulasri.jetinstagram.ui.search.Search
import com.vipulasri.jetinstagram.ui.profile.UserProfile
import com.vipulasri.jetinstagram.ui.profile.MyProfile
import com.vipulasri.jetinstagram.ui.profile.SelfProfile
import com.vipulasri.jetinstagram.ui.matching.Matching as MatchingScreen
import com.vipulasri.jetinstagram.ui.matching.Matching
import com.vipulasri.jetinstagram.ui.matching.MatchingInProgress
import com.vipulasri.jetinstagram.ui.matching.RandomVideoCallViewModel
import com.vipulasri.jetinstagram.data.RandomVideoCallRepository
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.ui.post.SinglePostScreen
import com.vipulasri.jetinstagram.data.SinglePostRepository
import com.vipulasri.jetinstagram.ui.videocall.VideoCallScreen
import com.vipulasri.jetinstagram.ui.profile.BlockedUsersScreen
import com.vipulasri.jetinstagram.ui.matching.RandomVideoCallUiState

enum class HomeSection(
    val icon: Int,
    val selectedIcon: Int
) {
    Home(R.drawable.ic_outlined_home, R.drawable.ic_filled_home),
    Matching(R.drawable.connection, R.drawable.connection),
    Add(R.drawable.upload, R.drawable.upload),
    Search(R.drawable.ic_outlined_search, R.drawable.ic_outlined_search),
    Profile(R.drawable.me, R.drawable.me)
}

@ExperimentalFoundationApi
@Composable
fun MainScreen() {

    val coroutineScope = rememberCoroutineScope()
    val sectionState = remember { mutableStateOf(HomeSection.Home) }
    val isViewingProfile = remember { mutableStateOf(false) }

    val isViewingMatchingInProgress = remember { mutableStateOf(false) }
    val isViewingSinglePost = remember { mutableStateOf(false) }
    val isViewingBlockedUsers = remember { mutableStateOf(false) }
    val isViewingVideoCall = remember { mutableStateOf(false) }
    val currentProfileUser = remember { mutableStateOf<User?>(null) }
    val currentPost = remember { mutableStateOf<com.vipulasri.jetinstagram.model.Post?>(null) }
    val searchQuery = remember { mutableStateOf("") }
    
    // Stack for managing overlays
    val overlayStack = remember { mutableStateListOf<Any>() }
    

    
    // Video call state
    val videoCallState = remember { mutableStateOf<RandomVideoCallUiState.Connected?>(null) }
    
    // Create shared ViewModel for the entire matching flow that persists across navigation
    val randomVideoCallViewModel = remember {
        RandomVideoCallViewModel(
            RandomVideoCallRepository(RetrofitInstance.api)
        )
    }
    
    // Current user (self) - use authenticated user info if available
    val currentUser = remember(AuthState.currentUserId, AuthState.currentUsername) {
        val username = AuthState.currentUsername ?: "johndoe"
        val name = AuthState.currentUsername?.let { "User $it" } ?: "John Doe"
        
        // Use AuthState.currentUserId directly, with a fallback only if it's null
        val userId = AuthState.currentUserId ?: run {
            val token = AuthState.currentToken
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    val jwtToken = token.substring(7) // Remove "Bearer " prefix
                    val parts = jwtToken.split(".")
                    if (parts.size == 3) {
                        val payload = parts[1]
                        // Add padding if needed
                        val paddedPayload = payload + "=".repeat((4 - payload.length % 4) % 4)
                        val decodedBytes = android.util.Base64.decode(paddedPayload, android.util.Base64.URL_SAFE)
                        val decodedPayload = String(decodedBytes)
                        println("MainScreen: JWT payload: $decodedPayload")
                        println("MainScreen: JWT payload length: ${decodedPayload.length}")
                        println("MainScreen: JWT payload contains 'userId': ${decodedPayload.contains("userId")}")
                        println("MainScreen: JWT payload contains 'sub': ${decodedPayload.contains("sub")}")
                        
                        // Try to extract user ID from payload (this is a simple approach)
                        // In a real app, you'd use a proper JWT library
                        println("MainScreen: JWT payload content: $decodedPayload")
                        
                        // Look for user ID in the payload
                        if (decodedPayload.contains("\"userId\":")) {
                            val userIdMatch = Regex("\"userId\":\\s*(\\d+)").find(decodedPayload)
                            if (userIdMatch != null) {
                                val extractedUserId = userIdMatch.groupValues[1].toLong()
                                println("MainScreen: Extracted userId from JWT: $extractedUserId")
                                extractedUserId
                            } else {
                                println("MainScreen: Could not extract userId from JWT, using fallback")
                                1L
                            }
                        } else if (decodedPayload.contains("\"sub\":")) {
                            // Check if sub contains a number that might be a user ID
                            val subMatch = Regex("\"sub\":\\s*\"([^\"]+)\"").find(decodedPayload)
                            if (subMatch != null) {
                                val subValue = subMatch.groupValues[1]
                                println("MainScreen: Found sub value in JWT: $subValue")
                                // If sub is a number, it might be a user ID
                                if (subValue.matches(Regex("\\d+"))) {
                                    val subUserId = subValue.toLong()
                                    println("MainScreen: Extracted userId from sub: $subUserId")
                                    subUserId
                                } else {
                                    println("MainScreen: Sub is not a number, using fallback")
                                    1L
                                }
                            } else {
                                println("MainScreen: Could not extract sub from JWT, using fallback")
                                1L
                            }
                        } else {
                            println("MainScreen: No userId or sub field found in JWT, using fallback")
                            1L
                        }
                    } else {
                        1L
                    }
                } catch (e: Exception) {
                    println("MainScreen: Error decoding JWT: ${e.message}")
                    1L
                }
            } else {
                1L
            }
        }
        
        println("MainScreen: Creating currentUser - username: $username, userId: $userId, AuthState.currentUserId: ${AuthState.currentUserId}")
        println("MainScreen: AuthState.currentToken: ${AuthState.currentToken?.take(50)}...")
        println("MainScreen: AuthState.isLoggedIn: ${AuthState.isLoggedIn}")
        User(
            id = userId,
            name = name,
            username = username,
            image = "https://randomuser.me/api/portraits/men/1.jpg"
        )
    }

    val navItems = HomeSection.values().toList()

    // Handle video call navigation
    if (isViewingVideoCall.value && videoCallState.value != null) {
        val connectedState = videoCallState.value!!
        VideoCallScreen(
            sessionId = connectedState.sessionId,
            roomId = connectedState.roomId,
            peerId = connectedState.peerId,
            signalingData = connectedState.signalingData,
            onEndCall = {
                isViewingVideoCall.value = false
                videoCallState.value = null
                randomVideoCallViewModel.reset()
            },
            onBackClick = {
                isViewingVideoCall.value = false
                videoCallState.value = null
                randomVideoCallViewModel.reset()
            }
        )
        return
    }

    Scaffold(
        bottomBar = {
            if (!isViewingSinglePost.value && !isViewingProfile.value) {
                BottomNavigation(
                    backgroundColor = Color.White,
                    elevation = 8.dp
                ) {
                    navItems.forEach { section ->
                        val isSelected = sectionState.value == section
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = if (isSelected) section.selectedIcon else section.icon),
                                    contentDescription = section.name,
                                    tint = if (isSelected) Color(0xFF6200EE) else Color.Gray
                                )
                            },
                            label = {
                                Text(
                                    text = section.name,
                                    color = if (isSelected) Color(0xFF6200EE) else Color.Gray
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                sectionState.value = section
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Global navigation states - these take precedence over section-specific content
            if (isViewingSinglePost.value) {
                currentPost.value?.let { post ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Base layer: Single Post Screen
                        SinglePostScreen(
                            post = post,
                            onBackClick = {
                                isViewingSinglePost.value = false
                                currentPost.value = null
                            },
                            onUserAvatarClick = { user ->
                                println("MainScreen: Avatar clicked for user: ${user.username} (current user: ${currentUser.username})")
                                // Check if it's the current user or another user
                                if (user.id == currentUser.id) {
                                    // Use overlay stack for current user (SelfProfile)
                                    overlayStack.add(user)
                                } else {
                                    // Use profile view for other users (UserProfile)
                                    currentProfileUser.value = user
                                    isViewingProfile.value = true
                                }
                            }
                        )
                        
                        // Render all overlays in stack order
                        overlayStack.forEachIndexed { index, overlay ->
                            when (overlay) {
                                is User -> {
                                    // Profile overlay - use SelfProfile for current user, UserProfile for others
                                    if (overlay.id == currentUser.id) {
                                        println("MainScreen: Rendering SelfProfile for current user: ${overlay.username}")
                                        SelfProfile(
                                            user = overlay,
                                            onBackClick = {
                                                // Remove this overlay from stack
                                                if (index < overlayStack.size) {
                                                    overlayStack.removeAt(index)
                                                }
                                            },
                                            onPostClick = { clickedPost ->
                                                // Add post to overlay stack
                                                overlayStack.add(clickedPost)
                                            },
                                            onUserAvatarClick = { clickedUser ->
                                                // Navigate to other users' profiles
                                                if (clickedUser.id != currentUser.id) {
                                                    overlayStack.add(clickedUser)
                                                }
                                            }
                                        )
                                    } else {
                                        println("MainScreen: Rendering UserProfile for other user: ${overlay.username}")
                                        UserProfile(
                                            user = overlay,
                                            onBackClick = {
                                                // Remove this overlay from stack
                                                if (index < overlayStack.size) {
                                                    overlayStack.removeAt(index)
                                                }
                                            },
                                            onPostClick = { clickedPost ->
                                                // Add post to overlay stack
                                                overlayStack.add(clickedPost)
                                            }
                                        )
                                    }
                                }
                                is com.vipulasri.jetinstagram.model.Post -> {
                                    // Post overlay
                                    SinglePostScreen(
                                        post = overlay,
                                        onBackClick = {
                                            // Remove this overlay from stack
                                            if (index < overlayStack.size) {
                                                overlayStack.removeAt(index)
                                            }
                                        },
                                        onUserAvatarClick = { user ->
                                            // Check if it's the current user or another user
                                            if (user.id == currentUser.id) {
                                                // Use overlay stack for current user (SelfProfile)
                                                overlayStack.add(user)
                                            } else {
                                                // Use profile view for other users (UserProfile)
                                                currentProfileUser.value = user
                                                isViewingProfile.value = true
                                            }
                                        },
                                        onPostClick = { clickedPost ->
                                            // Add post to overlay stack
                                            overlayStack.add(clickedPost)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (isViewingProfile.value) {
                currentProfileUser.value?.let { user ->
                    UserProfile(
                        user = user,
                        onBackClick = {
                            isViewingProfile.value = false
                            currentProfileUser.value = null
                        },
                        onPostClick = { post ->
                            currentPost.value = post
                            isViewingSinglePost.value = true
                        }
                    )
                }
            } else {
                // Section-specific content
                when (sectionState.value) {
                    HomeSection.Home -> {
                        Home(
                            onUserAvatarClick = { user ->
                                // Check if it's the current user or another user
                                if (user.id == currentUser.id) {
                                    // Use overlay stack for current user (SelfProfile)
                                    overlayStack.add(user)
                                } else {
                                    // Use profile view for other users (UserProfile)
                                    currentProfileUser.value = user
                                    isViewingProfile.value = true
                                }
                            },
                            onPostClick = { post ->
                                currentPost.value = post
                                isViewingSinglePost.value = true
                            }
                        )
                    }
                    HomeSection.Search -> {
                        Search(
                            initialQuery = searchQuery.value,
                            onUserAvatarClick = { user ->
                                // Check if it's the current user or another user
                                if (user.id == currentUser.id) {
                                    // Use overlay stack for current user (SelfProfile)
                                    overlayStack.add(user)
                                } else {
                                    // Use profile view for other users (UserProfile)
                                    currentProfileUser.value = user
                                    isViewingProfile.value = true
                                }
                            },
                            onPostClick = { post ->
                                currentPost.value = post
                                isViewingSinglePost.value = true
                            }
                        )
                    }
                    HomeSection.Matching -> {
                        if (isViewingMatchingInProgress.value) {
                            println("MainScreen: Showing MatchingInProgress screen")
                            MatchingInProgress(
                                viewModel = randomVideoCallViewModel,
                                onStopMatching = {
                                    isViewingMatchingInProgress.value = false
                                    randomVideoCallViewModel.reset()
                                },
                                onMatchFound = { connectedState ->
                                    // Handle when a match is found and connected
                                    println("Match found and connected with session: ${connectedState.sessionId}")
                                },
                                onCallConnected = { connectedState ->
                                    // Handle when call is connected
                                    println("Call connected with session: ${connectedState.sessionId}")
                                },
                                onNavigateToVideoCall = { connectedState ->
                                    // Navigate to video call screen
                                    videoCallState.value = connectedState
                                    isViewingVideoCall.value = true
                                }
                            )
                        } else {
                            println("MainScreen: Showing Matching screen")
                            Matching(
                                viewModel = randomVideoCallViewModel,
                                onStartMatching = {
                                    println("MainScreen: Setting isViewingMatchingInProgress to true")
                                    isViewingMatchingInProgress.value = true
                                    println("MainScreen: isViewingMatchingInProgress is now: ${isViewingMatchingInProgress.value}")
                                }
                            )
                        }
                    }
                    HomeSection.Add -> com.vipulasri.jetinstagram.ui.upload.Upload()
                    HomeSection.Profile -> {
                        if (isViewingBlockedUsers.value) {
                            BlockedUsersScreen(
                                onBackClick = {
                                    isViewingBlockedUsers.value = false
                                }
                            )
                        } else {
                            MyProfile(
                                user = currentUser,
                                onBackClick = { /* No action needed for main profile */ },
                                onPostClick = { post ->
                                    currentPost.value = post
                                    isViewingSinglePost.value = true
                                },
                                onBlockedUsersClick = {
                                    isViewingBlockedUsers.value = true
                                },
                                showBackButton = false
                            )
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
                    if (section == HomeSection.Profile) {
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

