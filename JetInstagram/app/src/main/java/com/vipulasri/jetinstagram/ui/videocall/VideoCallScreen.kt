package com.vipulasri.jetinstagram.ui.videocall

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun VideoCallScreen(
    matchId: Long,
    userId: Long,
    onEndCall: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var isMuted by remember { mutableStateOf(false) }
    var isVideoEnabled by remember { mutableStateOf(true) }
    var isCameraSwitched by remember { mutableStateOf(false) }
    var showEndCallDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Request permissions when screen loads
    LaunchedEffect(Unit) {
        // TODO: Request camera and microphone permissions
        println("Requesting camera and microphone permissions")
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main video area (remote user)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for remote video
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Remote User",
                        modifier = Modifier.size(80.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connecting...",
                        style = MaterialTheme.typography.h6,
                        color = Color.White
                    )
                }
            }
        }
        
        // Local video preview (small window)
        Box(
            modifier = Modifier
                .size(120.dp, 160.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            if (isVideoEnabled) {
                // Placeholder for local video
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Local Video",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Video Disabled",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Gray
                )
            }
        }
        
        // Call controls at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Call status
            Text(
                text = "Video Call",
                style = MaterialTheme.typography.h6,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mute button
                CallButton(
                    icon = Icons.Default.Call,
                    backgroundColor = if (isMuted) Color.Red else Color.White,
                    onClick = { isMuted = !isMuted }
                )
                
                // Video toggle button
                CallButton(
                    icon = Icons.Default.Call,
                    backgroundColor = if (isVideoEnabled) Color.White else Color.Red,
                    onClick = { isVideoEnabled = !isVideoEnabled }
                )
                
                // Camera switch button
                CallButton(
                    icon = Icons.Default.Call,
                    backgroundColor = Color.White,
                    onClick = { isCameraSwitched = !isCameraSwitched }
                )
                
                // End call button
                CallButton(
                    icon = Icons.Default.Call,
                    backgroundColor = Color.Red,
                    onClick = { showEndCallDialog = true }
                )
            }
        }
        
        // Back button at top
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
    
    // End call confirmation dialog
    if (showEndCallDialog) {
        Dialog(
            onDismissRequest = { showEndCallDialog = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "End Call",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "End Call?",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Are you sure you want to end this call?",
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showEndCallDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray
                            )
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                showEndCallDialog = false
                                onEndCall()
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Red
                            )
                        ) {
                            Text("End Call", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallButton(
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Call Control",
            modifier = Modifier.size(24.dp),
            tint = if (backgroundColor == Color.White) Color.Black else Color.White
        )
    }
} 