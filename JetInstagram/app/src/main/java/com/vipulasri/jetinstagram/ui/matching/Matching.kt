package com.vipulasri.jetinstagram.ui.matching

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vipulasri.jetinstagram.R
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.model.Match

@ExperimentalFoundationApi
@Composable
fun Matching(
    matches: List<Match> = emptyList(),
    users: List<User> = emptyList(),
    onLonelyMatchClick: () -> Unit = {},
    onStartVideoCall: (Long, Long) -> Unit = { _, _ -> },
    onStartVoiceCall: (Long, Long) -> Unit = { _, _ -> },
    onAcceptMatch: (Long) -> Unit = {},
    onDeclineMatch: (Long) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                elevation = 0.dp,
                title = {
                    Text(
                        text = "let's connect :)",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(onClick = onLonelyMatchClick) {
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
            if (matches.isEmpty()) {
                EmptyMatchesState(onLonelyMatchClick = onLonelyMatchClick)
            } else {
                MatchesList(
                    matches = matches,
                    users = users,
                    onStartVideoCall = onStartVideoCall,
                    onStartVoiceCall = onStartVoiceCall,
                    onAcceptMatch = onAcceptMatch,
                    onDeclineMatch = onDeclineMatch
                )
            }
        }
    }
}

@Composable
private fun EmptyMatchesState(onLonelyMatchClick: () -> Unit) {
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
            onClick = onLonelyMatchClick,
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
        Spacer(modifier = Modifier.height(32.dp))
        MatchingOptionsChart()
    }
}

@Composable
private fun MatchingOptionsChart() {
    var selectedPurpose by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Your Purpose Section
        Column {
            Text(
                text = "Your Purpose:",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OptionChip(
                    text = "Express",
                    isSelected = selectedPurpose == "Express",
                    onClick = { selectedPurpose = "Express" },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                OptionChip(
                    text = "Listen",
                    isSelected = selectedPurpose == "Listen",
                    onClick = { selectedPurpose = "Listen" },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
                OptionChip(
                    text = "Express and Listen",
                    isSelected = selectedPurpose == "Express and Listen",
                    onClick = { selectedPurpose = "Express and Listen" },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
        }
        
        // Match Someone Section
        Column {
            Text(
                text = "Match Someone...",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OptionChip(
                    text = "Near me",
                    isSelected = selectedLocation == "Near me",
                    onClick = { selectedLocation = "Near me" },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                OptionChip(
                    text = "Far away",
                    isSelected = selectedLocation == "Far away",
                    onClick = { selectedLocation = "Far away" },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
                OptionChip(
                    text = "Anywhere",
                    isSelected = selectedLocation == "Anywhere",
                    onClick = { selectedLocation = "Anywhere" },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun OptionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) Color(0xFF6200EE) else Color.White,
            contentColor = if (isSelected) Color.White else Color(0xFF6200EE)
        ),
        modifier = modifier.height(40.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.caption.copy(
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MatchesList(
    matches: List<Match>,
    users: List<User>,
    onStartVideoCall: (Long, Long) -> Unit,
    onStartVoiceCall: (Long, Long) -> Unit,
    onAcceptMatch: (Long) -> Unit,
    onDeclineMatch: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(matches) { match ->
            val matchedUser = users.find { it.id == match.matchedUserId }
            matchedUser?.let { user ->
                MatchCard(
                    user = user,
                    matchId = match.matchId,
                    matchStatus = match.matchStatus,
                    onAcceptMatch = onAcceptMatch,
                    onDeclineMatch = onDeclineMatch,
                    onStartVideoCall = onStartVideoCall,
                    onStartVoiceCall = onStartVoiceCall,
                    showAcceptDeclineButtons = false
                )
            }
        }
    }
} 