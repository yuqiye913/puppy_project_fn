package com.vipulasri.jetinstagram.ui.matching

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vipulasri.jetinstagram.network.RandomVideoCallStatistics

@ExperimentalFoundationApi
@Composable
fun MatchingInProgress(
    viewModel: RandomVideoCallViewModel,
    onStopMatching: () -> Unit = {},
    onMatchFound: (RandomVideoCallUiState.Connected) -> Unit = {},
    onCallConnected: (RandomVideoCallUiState.Connected) -> Unit = {},
    onNavigateToVideoCall: (RandomVideoCallUiState.Connected) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val queueStatistics by viewModel.queueStatistics.collectAsState()
    
    // Debug logging
    LaunchedEffect(uiState) {
        println("MatchingInProgress: Current state is: $uiState")
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadQueueStatistics()
        
        // First, try to load any existing active request
        viewModel.loadCurrentUserActiveRequest()
        
        // Start a new request if we're in idle state (no active request found)
        if (uiState is RandomVideoCallUiState.Idle) {
            val currentUserId = com.vipulasri.jetinstagram.ui.auth.AuthState.currentUserId
            if (currentUserId != null) {
                println("MatchingInProgress: Auto-starting random video call for user $currentUserId")
                viewModel.startRandomVideoCall(currentUserId)
            } else {
                println("MatchingInProgress: Cannot start video call - user not logged in")
                // TODO: Navigate back or show error
            }
        }
    }
    
    // Handle state changes
    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is RandomVideoCallUiState.Connected -> {
                onMatchFound(currentState)
                onCallConnected(currentState)
                // Navigate to video call screen
                onNavigateToVideoCall(currentState)
            }
            is RandomVideoCallUiState.Error -> {
                // Handle error - could show a snackbar or dialog
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
                    // Removed the stop matching button from top bar to avoid redundancy
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
            when (val currentState = uiState) {
                is RandomVideoCallUiState.Idle -> {
                    // Show requesting content even in idle state
                    RequestingContent(
                        onStopMatching = {
                            viewModel.cancelRandomVideoCall()
                            onStopMatching()
                        }
                    )
                }
                
                is RandomVideoCallUiState.Requesting -> {
                    RequestingContent(
                        onStopMatching = {
                            viewModel.cancelRandomVideoCall()
                            onStopMatching()
                        }
                    )
                }
                
                is RandomVideoCallUiState.Waiting -> {
                    WaitingContent(
                        queuePosition = currentState.queuePosition,
                        estimatedWaitTime = currentState.estimatedWaitTime,
                        totalUsersInQueue = currentState.totalUsersInQueue,
                        queueStatistics = queueStatistics,
                        onStopMatching = {
                            viewModel.cancelRandomVideoCall()
                            onStopMatching()
                        }
                    )
                }
                
                is RandomVideoCallUiState.Connected -> {
                    // Show a brief connected message before navigating
                    ConnectedContent(
                        matchedDisplayName = currentState.matchedDisplayName,
                        matchScore = currentState.matchScore,
                        onEndCall = { viewModel.endCall() }
                    )
                }
                
                is RandomVideoCallUiState.Declined -> {
                    DeclinedContent(
                        onStartNewCall = { viewModel.reset() }
                    )
                }
                
                is RandomVideoCallUiState.Timeout -> {
                    TimeoutContent(
                        onStartNewCall = { viewModel.reset() }
                    )
                }
                
                is RandomVideoCallUiState.Cancelled -> {
                    CancelledContent(
                        onStartNewCall = { viewModel.reset() }
                    )
                }
                
                is RandomVideoCallUiState.Ended -> {
                    EndedContent(
                        onStartNewCall = { viewModel.reset() }
                    )
                }
                
                is RandomVideoCallUiState.Error -> {
                    ErrorContent(
                        errorMessage = currentState.message,
                        onRetry = { viewModel.reset() }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestingContent(
    onStopMatching: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(120.dp),
            color = Color(0xFF6200EE)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Requesting random video call...",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stop matching button
        Button(
            onClick = onStopMatching,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Stop Matching",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Stop Matching",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WaitingContent(
    queuePosition: Long,
    estimatedWaitTime: Long,
    totalUsersInQueue: Long,
    queueStatistics: RandomVideoCallStatistics?,
    onStopMatching: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Matching animation icon
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Searching for matches",
            modifier = Modifier.size(120.dp),
            tint = Color(0xFF6200EE)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Finding matches...",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "We're looking for people who match your interests",
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Queue information
        Card(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Queue Position: $queuePosition",
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Estimated wait: ${estimatedWaitTime / 60} minutes",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Total users in queue: $totalUsersInQueue",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
                
                queueStatistics?.let { stats ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Average wait time: ${stats.averageWaitTime / 60} minutes",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stop matching button
        Button(
            onClick = onStopMatching,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Stop Matching",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Stop Matching",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Composable
private fun ConnectedContent(
    matchedDisplayName: String,
    matchScore: Double,
    onEndCall: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Video Call Active",
            modifier = Modifier.size(120.dp),
            tint = Color.Green
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Video Call Connected!",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You are now connected with $matchedDisplayName",
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Match Score: ${(matchScore * 100).toInt()}%",
            style = MaterialTheme.typography.body2,
            color = Color(0xFF6200EE),
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onEndCall,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "End Call",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "End Call",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DeclinedContent(
    onStartNewCall: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Call Declined",
            modifier = Modifier.size(120.dp),
            tint = Color(0xFFFF9800)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Call Declined",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You declined the match",
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStartNewCall,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE)
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Start New Call",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TimeoutContent(
    onStartNewCall: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Timeout",
            modifier = Modifier.size(120.dp),
            tint = Color(0xFFFF9800)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Search Timeout",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No matches found within the time limit",
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStartNewCall,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE)
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Try Again",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CancelledContent(
    onStartNewCall: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cancelled",
            modifier = Modifier.size(120.dp),
            tint = Color.Red
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Search Cancelled",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You cancelled the search",
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStartNewCall,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE)
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Start New Search",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EndedContent(
    onStartNewCall: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Call Ended",
            modifier = Modifier.size(120.dp),
            tint = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Call Ended",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "The video call has ended",
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onStartNewCall,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE)
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Start New Call",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            modifier = Modifier.size(120.dp),
            tint = Color.Red
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Error",
            style = MaterialTheme.typography.h5.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.body1,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF6200EE)
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Retry",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
} 