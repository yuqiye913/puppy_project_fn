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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import coil.compose.rememberImagePainter
import com.vipulasri.jetinstagram.R
import com.vipulasri.jetinstagram.data.BlockRepository
import com.vipulasri.jetinstagram.data.BlockedUser
import kotlinx.coroutines.launch
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalFoundationApi
@Composable
fun BlockedUsersScreen(
    onBackClick: () -> Unit = {}
) {
    val blockedUsers by BlockRepository.blockedUsers
    val isLoading by BlockRepository.isLoading
    val error by BlockRepository.error
    val hasMorePages by BlockRepository.hasMorePages
    
    // Debug logging
    LaunchedEffect(blockedUsers) {
        println("BlockedUsersScreen: Blocked users list updated. Count: ${blockedUsers.size}")
        blockedUsers.forEach { user ->
            println("BlockedUsersScreen: User ${user.username} (ID: ${user.userId})")
        }
    }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Load blocked users when component is first created
    LaunchedEffect(Unit) {
        println("BlockedUsersScreen: Loading blocked users...")
        println("BlockedUsersScreen: AuthState.isLoggedIn: ${com.vipulasri.jetinstagram.ui.auth.AuthState.isLoggedIn}")
        println("BlockedUsersScreen: AuthState.currentToken: ${com.vipulasri.jetinstagram.ui.auth.AuthState.currentToken != null}")
        BlockRepository.loadBlockedUsers(refresh = true)
    }
    
    // Handle pagination when user scrolls to the bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                if (visibleItems.isNotEmpty()) {
                    val lastVisibleItem = visibleItems.last()
                    val totalItems = blockedUsers.size
                    
                    if (lastVisibleItem.index >= totalItems - 3 && hasMorePages && !isLoading) {
                        scope.launch {
                            BlockRepository.loadBlockedUsers(refresh = false)
                        }
                    }
                }
            }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Blocked Users",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // Show error if any
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    backgroundColor = Color(0xFFFFEBEE),
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = ImageBitmap.imageResource(id = R.drawable.block),
                            contentDescription = "Error",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
            
            if (blockedUsers.isEmpty() && !isLoading) {
                EmptyBlockedUsersState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(blockedUsers) { blockedUser ->
                        BlockedUserItem(
                            blockedUser = blockedUser,
                            onUnblock = { userId ->
                                println("BlockedUsersScreen: Unblock button clicked for user ID: $userId")
                                scope.launch {
                                    BlockRepository.unblockUser(
                                        userId = userId,
                                        onSuccess = {
                                            println("BlockedUsersScreen: Unblock successful for user ID: $userId")
                                            Toast.makeText(context, "User unblocked successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { error ->
                                            println("BlockedUsersScreen: Unblock failed for user ID: $userId - $error")
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        )
                    }
                    
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedUserItem(
    blockedUser: BlockedUser,
    onUnblock: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color = Color.LightGray, shape = CircleShape)
                    .clip(CircleShape)
            ) {
                if (blockedUser.profilePicture != null) {
                    Image(
                        painter = rememberImagePainter(blockedUser.profilePicture),
                        contentDescription = "Profile picture of ${blockedUser.username}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Default profile picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = blockedUser.displayName ?: blockedUser.username,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
                
                Text(
                    text = "@${blockedUser.username}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                
                blockedUser.reason?.let { reason ->
                    if (reason.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reason: $reason",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Blocked on ${formatDate(blockedUser.blockedAt)}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }
            
            // Unblock Button
            Button(
                onClick = { onUnblock(blockedUser.userId) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Unblock",
                    style = MaterialTheme.typography.body2.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EmptyBlockedUsersState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.block),
                contentDescription = "No blocked users",
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Blocked Users",
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You haven't blocked any users yet. Blocked users will appear here.",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
} 