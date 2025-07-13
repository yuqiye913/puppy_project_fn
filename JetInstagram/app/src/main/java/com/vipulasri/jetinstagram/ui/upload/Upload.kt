package com.vipulasri.jetinstagram.ui.upload

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vipulasri.jetinstagram.R
import androidx.compose.material.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.vipulasri.jetinstagram.network.PostUploadRequest
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.network.ErrorHandler
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.layout.Box

@Composable
fun HashtagHighlightedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val annotatedText = buildAnnotatedString {
        val hashtagRegex = Regex("#([a-zA-Z0-9_]+)")
        var lastIndex = 0
        
        hashtagRegex.findAll(value).forEach { matchResult ->
            // Add text before hashtag
            append(value.substring(lastIndex, matchResult.range.first))
            
            // Add hashtag with blue color
            withStyle(SpanStyle(color = Color.Blue)) {
                append(matchResult.value)
            }
            
            lastIndex = matchResult.range.last + 1
        }
        
        // Add remaining text
        if (lastIndex < value.length) {
            append(value.substring(lastIndex))
        }
    }
    
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = MaterialTheme.colors.onSurface,
            fontSize = MaterialTheme.typography.body1.fontSize
        ),
        modifier = modifier
            .background(
                color = MaterialTheme.colors.surface,
                shape = MaterialTheme.shapes.small
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.small
            )
            .padding(16.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.body1.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun Upload() {
    var story by remember { mutableStateOf("") }
    var postName by remember { mutableStateOf("") }
    var detectedHashtags by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val storyFocusRequester = remember { FocusRequester() }
    
    // Function to extract hashtags from text
    fun extractHashtags(text: String): List<String> {
        // Regex to match hashtags: # followed by letters, numbers, or underscores
        // Stops at spaces, punctuation, or end of string
        val hashtagRegex = Regex("#([a-zA-Z0-9_]+)")
        return hashtagRegex.findAll(text)
            .map { it.groupValues[1] }
            .filter { it.isNotEmpty() } // Filter out empty matches
            .distinct() // Remove duplicates
            .toList()
    }
    
    // Function to add hashtag to story
    fun addHashtag() {
        story += "#"
        // Focus on the story text field after adding hashtag
        storyFocusRequester.requestFocus()
    }
    
    // Update detected hashtags when story changes
    LaunchedEffect(story) {
        detectedHashtags = extractHashtags(story)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Express your feeling now :)",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
        )
        
        // Post Name Field
        TextField(
            value = postName,
            onValueChange = { postName = it },
            placeholder = { Text("Post title") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // Story/Description Field
        HashtagHighlightedTextField(
            value = story,
            onValueChange = { story = it },
            placeholder = "write your story here",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .focusRequester(storyFocusRequester)
        )
        
        // Helper text for hashtags
        Text(
            text = "Tip: Use #hashtags in your story to automatically create subreddits",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
        
        // Display detected hashtags
        if (detectedHashtags.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Detected hashtags (will be used as subreddits):",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = detectedHashtags.joinToString(", ") { "#$it" },
                    style = MaterialTheme.typography.body2,
                    color = androidx.compose.ui.graphics.Color.Blue
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /* TODO: Handle location */ }) {
                Text("Your Location")
            }
            Button(onClick = { addHashtag() }) {
                Text("#Hashtag")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        var friendsOnly by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = friendsOnly,
                onCheckedChange = { friendsOnly = it }
            )
            Text(
                text = "Friends Only",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                scope.launch {
                    val token = AuthState.currentToken?.let { "Bearer $it" }
                    if (token == null) {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    
                    // Validate required fields
                    if (postName.trim().isEmpty()) {
                        Toast.makeText(context, "Post title cannot be empty", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    
                    // Use detected hashtags as subreddits
                    val allSubreddits = detectedHashtags.toMutableList()
                    
                    // Ensure at least one subreddit is provided
                    if (allSubreddits.isEmpty()) {
                        Toast.makeText(context, "Please add at least one hashtag to your story", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    
                    val request = PostUploadRequest(
                        postName = postName.trim(),
                        url = null,
                        description = story,
                        subredditNames = allSubreddits,
                        hashtags = detectedHashtags // Send hashtags separately as well
                    )
                    try {
                        val response = RetrofitInstance.api.createPost(token, request)
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                            // Clear form after successful upload
                            postName = ""
                            story = ""
                            detectedHashtags = listOf()
                        } else {
                            // Use ErrorHandler to parse and display error message
                            val errorMessage = ErrorHandler.parseErrorResponse(response)
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                painter = painterResource(id = R.drawable.star),
                contentDescription = "Star",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colors.onPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Upload Now",
                style = MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onPrimary)
            )
        }
    }
} 