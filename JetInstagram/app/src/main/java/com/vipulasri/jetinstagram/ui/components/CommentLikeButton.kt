package com.vipulasri.jetinstagram.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.vipulasri.jetinstagram.R
import com.vipulasri.jetinstagram.model.Comment

enum class CommentLikeAnimationState {
    Initial,
    Start,
    End
}

private const val springRatio = Spring.DampingRatioHighBouncy

@Composable
fun CommentLikeButton(
    comment: Comment,
    onLikeClick: (Comment) -> Unit,
    onLikeToggle: ((Long, Boolean) -> Unit)? = null
) {
    var transitionState by remember {
        mutableStateOf(MutableTransitionState(CommentLikeAnimationState.Initial))
    }

    Box(
        modifier = Modifier
            .clickable(
                onClick = {
                    transitionState = MutableTransitionState(CommentLikeAnimationState.Start)
                    onLikeClick.invoke(comment)
                    // Call the like/unlike API based on current state
                    onLikeToggle?.invoke(comment.id.toLong(), !comment.isLiked)
                }
            )
            .indication(
                indication = rememberRipple(bounded = false, radius = 16.dp),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 4.dp)
            .then(Modifier.size(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        val likeIconRes = if (comment.isLiked) {
            R.drawable.ic_filled_favorite
        } else {
            R.drawable.ic_outlined_favorite
        }

        val iconColor = if (comment.isLiked) {
            Color.Red // Bright red color when liked
        } else {
            Color.Gray // Gray color when not liked
        }

        if (transitionState.currentState == CommentLikeAnimationState.Start) {
            transitionState.targetState = CommentLikeAnimationState.End
        }

        val transition = updateTransition(transitionState, label = "")

        val size by transition.animateDp(
            transitionSpec = {
                when {
                    CommentLikeAnimationState.Initial isTransitioningTo CommentLikeAnimationState.Start ->
                        spring(dampingRatio = springRatio)
                    CommentLikeAnimationState.Start isTransitioningTo CommentLikeAnimationState.End ->
                        tween(200)
                    else -> snap()
                }
            }, label = ""
        ) { state ->
            when (state) {
                CommentLikeAnimationState.Initial -> 16.dp
                CommentLikeAnimationState.Start -> 12.dp
                CommentLikeAnimationState.End -> 16.dp
            }
        }

        Icon(
            ImageBitmap.imageResource(id = likeIconRes),
            tint = iconColor,
            modifier = Modifier.size(size),
            contentDescription = "Like comment"
        )
    }
} 