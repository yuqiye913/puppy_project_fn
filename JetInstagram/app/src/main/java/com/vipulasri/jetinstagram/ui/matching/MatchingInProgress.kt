package com.vipulasri.jetinstagram.ui.matching

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.vipulasri.jetinstagram.R

@Composable
fun MatchingInProgress(onStopMatching: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F0FF)), // soft blue
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = "Star",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "we are matching now",
                    style = MaterialTheme.typography.h5.copy(color = Color(0xFF1A237E))
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onStopMatching,
                modifier = Modifier
                    .width(240.dp)
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Stop Matching",
                    style = MaterialTheme.typography.h6.copy(
                        color = Color.White
                    )
                )
            }
        }
    }
} 