package com.vipulasri.jetinstagram.ui.matching

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.ui.auth.AuthState

@Composable
fun MatchCard(
    user: User,
    onStartVideoCall: (Long, Long) -> Unit = { _, _ -> },
    onStartVoiceCall: (Long, Long) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF1976D2)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Name
            Text(
                text = user.name,
                style = MaterialTheme.typography.h6.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            // Username
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Call Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Video Call Button
                Button(
                    onClick = { 
                        val currentUserId = AuthState.currentUserId
                        if (currentUserId != null) {
                            onStartVideoCall(currentUserId, user.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFE91E63)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Start Video Call",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Video Call",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Voice Call Button
                Button(
                    onClick = { 
                        val currentUserId = AuthState.currentUserId
                        if (currentUserId != null) {
                            onStartVoiceCall(currentUserId, user.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF9C27B0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Voice Call",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Call",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 