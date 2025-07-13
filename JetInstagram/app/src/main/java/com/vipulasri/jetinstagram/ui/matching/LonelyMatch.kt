package com.vipulasri.jetinstagram.ui.matching

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vipulasri.jetinstagram.R

@ExperimentalFoundationApi
@Composable
fun LonelyMatch(onBackClick: () -> Unit = {}, onStartMatchingNow: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                title = {
                    Text(
                        text = "Lonely Match",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lonely Match",
                    style = MaterialTheme.typography.h4.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Find your perfect match!",
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = "Star",
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "....loners online now",
                        style = MaterialTheme.typography.h6.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onStartMatchingNow,
                    modifier = Modifier
                        .width(220.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6200EE)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Starting Match Now",
                        style = MaterialTheme.typography.button.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                // --- Purpose Chart ---
                var purpose by remember { mutableStateOf("Express") }
                Text(
                    text = "Your Purpose:",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start).padding(start = 32.dp, bottom = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Express", "Listen", "Express and Listen").forEach { option ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { purpose = option }
                                .background(
                                    if (purpose == option) Color(0xFFE3D7FF) else Color.White
                                ),
                            elevation = if (purpose == option) 6.dp else 2.dp,
                            border = if (purpose == option) BorderStroke(2.dp, Color(0xFF6200EE)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = purpose == option,
                                    onClick = { purpose = option },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6200EE))
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                // --- Match Someone Chart ---
                var matchPref by remember { mutableStateOf("Near me") }
                Text(
                    text = "Match Someone...",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start).padding(start = 32.dp, bottom = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Near me", "Far away", "Anywhere").forEach { option ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { matchPref = option }
                                .background(
                                    if (matchPref == option) Color(0xFFE3D7FF) else Color.White
                                ),
                            elevation = if (matchPref == option) 6.dp else 2.dp,
                            border = if (matchPref == option) BorderStroke(2.dp, Color(0xFF6200EE)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = matchPref == option,
                                    onClick = { matchPref = option },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6200EE))
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
} 