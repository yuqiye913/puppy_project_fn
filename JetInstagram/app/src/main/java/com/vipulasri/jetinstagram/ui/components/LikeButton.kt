package com.vipulasri.jetinstagram.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.vipulasri.jetinstagram.R
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.model.names

enum class LikeAnimationState {
  Initial,
  Start,
  End
}

private const val springRatio = Spring.DampingRatioHighBouncy

@Composable
fun AnimLikeButton(
  post: Post,
  onLikeClick: (Post) -> Unit,
  onLikeToggle: ((Long, Boolean) -> Unit)? = null
) {

  var transitionState by remember {
    mutableStateOf(MutableTransitionState(LikeAnimationState.Initial))
  }

  Box(
    modifier = Modifier
      .clickable(
        onClick = {
          transitionState = MutableTransitionState(LikeAnimationState.Start)
          onLikeClick.invoke(post)
          // Call the like/unlike API based on current state
          onLikeToggle?.invoke(post.id.toLong(), !post.isLiked)
        }
      )
      .indication(
        indication = rememberRipple(bounded = false, radius = 24.dp),
        interactionSource = remember { MutableInteractionSource() }
      )
      .padding(vertical = 10.dp)
      .then(Modifier.size(30.dp)),
    contentAlignment = Alignment.Center
  ) {

    val likeIconRes = if (post.isLiked) {
      R.drawable.ic_filled_favorite
    } else {
      R.drawable.ic_outlined_favorite
    }

    val iconColor = if (post.isLiked) {
      println("DEBUG: Post ${post.id} is liked, using RED color")
      Color.Red // Bright red color when liked
    } else {
      println("DEBUG: Post ${post.id} is NOT liked, using GRAY color")
      Color.Gray // Gray color when not liked
    }

    if (transitionState.currentState == LikeAnimationState.Start) {
      transitionState.targetState = LikeAnimationState.End
    }

    val transition = updateTransition(transitionState, label = "")

    val size by transition.animateDp(
      transitionSpec = {
        when {
          LikeAnimationState.Initial isTransitioningTo LikeAnimationState.Start ->
            spring(dampingRatio = springRatio)
          LikeAnimationState.Start isTransitioningTo LikeAnimationState.End ->
            tween(200)
          else -> snap()
        }
      }, label = ""
    ) { state ->
      when (state) {
        LikeAnimationState.Initial -> 24.dp
        LikeAnimationState.Start -> 12.dp
        LikeAnimationState.End -> 24.dp
      }
    }

    Icon(
      ImageBitmap.imageResource(id = likeIconRes), 
      tint = iconColor,
      modifier = Modifier.size(size),
      contentDescription = ""
    )
  }
}

@Preview
@Composable
private fun LikeButtonPreview() {
  AnimLikeButton(
    post = Post(
      id = 1,
      title = "Coffee Post", // Add title
      text = "Just had the most amazing coffee this morning! ☕️",
      user = User(
        name = names.first(),
        username = names.first(),
        image = "https://randomuser.me/api/portraits/men/1.jpg"
      ),
      likesCount = 100,
      commentsCount = 20,
      timeStamp = System.currentTimeMillis() - (60000),
      bestComment = null // Remove mock comment
    ),
    onLikeClick = {
    })
}