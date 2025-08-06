package com.vipulasri.jetinstagram.ui.matching

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.model.Match
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun MatchingInProgress(
    onStopMatching: () -> Unit = {},
    onAcceptMatch: (Long) -> Unit = {},
    onDeclineMatch: (Long) -> Unit = {},
    onStartVideoCall: (Long, Long) -> Unit = { _, _ -> },
    onStartVoiceCall: (Long, Long) -> Unit = { _, _ -> }
) {
    // Sample data for demonstration - in real app, this would come from API
    val sampleMatches = remember {
        listOf(
            Match(
                matchId = 1L,
                userId = 1L,
                matchedUserId = 2L,
                matchStatus = "pending"
            ),
            Match(
                matchId = 2L,
                userId = 1L,
                matchedUserId = 3L,
                matchStatus = "pending"
            ),
            Match(
                matchId = 3L,
                userId = 1L,
                matchedUserId = 4L,
                matchStatus = "accepted"
            )
        )
    }
    
    val sampleUsers = remember {
        listOf(
            User(id = 2, name = "Sarah Johnson", username = "sarah_j", image = ""),
            User(id = 3, name = "Mike Chen", username = "mike_c", image = ""),
            User(id = 4, name = "Emma Davis", username = "emma_d", image = "")
        )
    }
    
    var matches by remember { mutableStateOf(sampleMatches) }
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                elevation = 0.dp,
                title = {
                    Text(
                        text = "We are matching now",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(onClick = onStopMatching) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop Matching",
                            tint = Color.Red
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
            val pendingMatches = matches.filter { it.matchStatus == "pending" }
            val acceptedMatches = matches.filter { it.matchStatus == "accepted" }
            
            if (pendingMatches.isEmpty() && acceptedMatches.isEmpty()) {
                // No matches yet - show matching animation
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
                        text = "Searching for matches...",
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "We're looking for people who match your interests!",
                        style = MaterialTheme.typography.body1,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
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
            } else {
                // Show matches
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pending matches section
                    if (pendingMatches.isNotEmpty()) {
                        item {
                            Text(
                                text = "New Match Requests",
                                style = MaterialTheme.typography.h6.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(pendingMatches) { match ->
                            val matchedUser = sampleUsers.find { it.id == match.matchedUserId }
                            matchedUser?.let { user ->
                                MatchCard(
                                    user = user,
                                    matchId = match.matchId,
                                    matchStatus = match.matchStatus,
                                    onAcceptMatch = { matchId ->
                                        coroutineScope.launch {
                                            // TODO: Call API to accept match
                                            matches = matches.map { 
                                                if (it.matchId == matchId) {
                                                    it.copy(matchStatus = "accepted")
                                                } else it
                                            }
                                        }
                                    },
                                    onDeclineMatch = { matchId ->
                                        coroutineScope.launch {
                                            // TODO: Call API to decline match
                                            matches = matches.map { 
                                                if (it.matchId == matchId) {
                                                    it.copy(matchStatus = "declined")
                                                } else it
                                            }
                                        }
                                    },
                                    onStartVideoCall = { matchId, userId ->
                                        onStartVideoCall(matchId, userId)
                                    },
                                    onStartVoiceCall = { matchId, userId ->
                                        onStartVoiceCall(matchId, userId)
                                    },
                                    showAcceptDeclineButtons = true
                                )
                            }
                        }
                    }
                    
                    // Accepted matches section
                    if (acceptedMatches.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your Matches",
                                style = MaterialTheme.typography.h6.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(acceptedMatches) { match ->
                            val matchedUser = sampleUsers.find { it.id == match.matchedUserId }
                            matchedUser?.let { user ->
                                MatchCard(
                                    user = user,
                                    matchId = match.matchId,
                                    matchStatus = match.matchStatus,
                                    onAcceptMatch = { matchId ->
                                        coroutineScope.launch {
                                            // TODO: Call API to accept match
                                            matches = matches.map { 
                                                if (it.matchId == matchId) {
                                                    it.copy(matchStatus = "accepted")
                                                } else it
                                            }
                                        }
                                    },
                                    onDeclineMatch = { matchId ->
                                        coroutineScope.launch {
                                            // TODO: Call API to decline match
                                            matches = matches.map { 
                                                if (it.matchId == matchId) {
                                                    it.copy(matchStatus = "declined")
                                                } else it
                                            }
                                        }
                                    },
                                    onStartVideoCall = { matchId, userId ->
                                        onStartVideoCall(matchId, userId)
                                    },
                                    onStartVoiceCall = { matchId, userId ->
                                        onStartVoiceCall(matchId, userId)
                                    },
                                    showAcceptDeclineButtons = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 