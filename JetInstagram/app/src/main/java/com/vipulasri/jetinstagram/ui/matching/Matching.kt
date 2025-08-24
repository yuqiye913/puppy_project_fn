package com.vipulasri.jetinstagram.ui.matching

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.vipulasri.jetinstagram.network.RandomVideoCallStatistics
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vipulasri.jetinstagram.R
import com.vipulasri.jetinstagram.ui.auth.AuthState

@ExperimentalFoundationApi
@Composable
fun Matching(
    viewModel: RandomVideoCallViewModel,
    onStartMatching: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is RandomVideoCallUiState.Requesting -> {
                onStartMatching()
            }
            is RandomVideoCallUiState.Waiting -> {
                onStartMatching()
            }
            else -> {
                // Handle other states
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                elevation = 0.dp,
                title = {
                    Text(
                        text = "Matching",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(onClick = { /* Could show settings or help */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.star),
                            contentDescription = "Start Matching",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No matches yet",
                    style = MaterialTheme.typography.h6,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start matching to find new connections!",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        // Get current user ID and start random video call
                        val currentUserId = AuthState.currentUserId
                        if (currentUserId != null) {
                            println("Matching: Starting random video call for user $currentUserId")
                            viewModel.startRandomVideoCall(currentUserId)
                        } else {
                            println("Matching: Cannot start video call - user not logged in")
                            // TODO: Show error message or redirect to login
                            return@Button
                        }
                        // Immediately navigate to matching in progress
                        println("Matching: Calling onStartMatching()")
                        onStartMatching()
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6200EE)
                    ),
                    modifier = Modifier
                        .height(72.dp)
                        .padding(horizontal = 48.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = "Start Matching",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Start Matching",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show loading state if requesting
                if (uiState is RandomVideoCallUiState.Requesting) {
                    CircularProgressIndicator(
                        color = Color(0xFF6200EE),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Requesting random video call...",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
                
                // Show error if any
                if (uiState is RandomVideoCallUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (uiState as RandomVideoCallUiState.Error).message,
                        style = MaterialTheme.typography.body2,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
} 