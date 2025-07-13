package com.vipulasri.jetinstagram.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.vipulasri.jetinstagram.R
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User

@ExperimentalFoundationApi
@Composable
fun Home(
    onUserAvatarClick: ((User) -> Unit)? = null,
    onHashtagClick: ((String) -> Unit)? = null,
    onPostClick: ((Post) -> Unit)? = null
) {

  Scaffold(
    topBar = { Toolbar() }) {

    LazyColumn {
      // Promoted Posts Section
      item {
        PromotedPostsSection(
          onUserAvatarClick = onUserAvatarClick,
          onPostClick = onPostClick,
          onHashtagClick = onHashtagClick
        )
      }
      
      // Add a message when no promoted posts are available
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No posts available. Create your first post!",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
          )
        }
      }
    }
  }
}

@Composable
private fun Toolbar() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp)
      .padding(horizontal = 10.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      ImageBitmap.imageResource(id = R.drawable.ic_outlined_home),
      modifier = Modifier.size(32.dp),
      contentDescription = "Home Icon"
    )
  }
}