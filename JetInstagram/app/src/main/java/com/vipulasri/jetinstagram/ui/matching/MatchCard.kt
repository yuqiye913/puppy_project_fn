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

@Composable
fun MatchCard(
    user: User,
    matchId: Long? = null,
    matchStatus: String = "pending",
    onAcceptMatch: (Long) -> Unit = {},
    onDeclineMatch: (Long) -> Unit = {},
    onStartVideoCall: (Long, Long) -> Unit = { _, _ -> },
    onStartVoiceCall: (Long, Long) -> Unit = { _, _ -> },
    showAcceptDeclineButtons: Boolean = false
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
            
            // Call to action for pending matches
            if (matchStatus == "pending") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the star to accept and start calling! ⭐",
                    style = MaterialTheme.typography.caption,
                    color = Color(0xFF6200EE),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Username
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Match Status
            when (matchStatus) {
                "pending" -> {
                    if (showAcceptDeclineButtons) {
                        // Accept/Decline Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { 
                                    matchId?.let { onAcceptMatch(it) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFFFFD700) // Gold color for star
                                ),
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Accept Match",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Accept & Call",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Button(
                                onClick = { 
                                    matchId?.let { onDeclineMatch(it) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFFF44336)
                                ),
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Decline",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Decline",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Show pending status without buttons
                        Text(
                            text = "Match Request Pending",
                            style = MaterialTheme.typography.subtitle1,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                "accepted" -> {
                    // Video Call and Voice Call Buttons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Success animation indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Accepted",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Match Accepted! 🎉",
                                style = MaterialTheme.typography.subtitle1.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF4CAF50),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Prominent Video Call Button
                        Button(
                            onClick = { 
                                matchId?.let { onStartVideoCall(it, user.id) }
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFE91E63) // Pink color for video call
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
                                text = "Start Video Call",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Voice Call Button
                        Button(
                            onClick = { 
                                matchId?.let { onStartVoiceCall(it, user.id) }
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
                "declined" -> {
                    Text(
                        text = "Match Declined",
                        style = MaterialTheme.typography.subtitle1,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Text(
                        text = "Match Status: $matchStatus",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 