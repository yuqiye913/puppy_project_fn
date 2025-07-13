package com.vipulasri.jetinstagram.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.vipulasri.jetinstagram.R.drawable
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.ui.components.*
import com.vipulasri.jetinstagram.model.Comment
import com.vipulasri.jetinstagram.model.User

@ExperimentalFoundationApi
@Composable
fun PostView(
  post: Post,
  onDoubleClick: (Post) -> Unit,
  onLikeToggle: (Post) -> Unit,
  onUserAvatarClick: ((User) -> Unit)? = null,
  onHashtagClick: ((String) -> Unit)? = null,
  onLikeToggleApi: ((Long, Boolean) -> Unit)? = null,
  onCommentClick: ((Post) -> Unit)? = null
) {
  Column {
    PostHeader(post, onUserAvatarClick)
    PostContent(post, onDoubleClick, onHashtagClick)
    PostFooter(post, onLikeToggle, onLikeToggleApi, onCommentClick)
    post.bestComment?.let { comment ->
      BestCommentSection(comment, onUserAvatarClick, onHashtagClick)
    }
    Divider()
  }
}

@Composable
private fun PostHeader(post: Post, onUserAvatarClick: ((User) -> Unit)? = null) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .defaultPadding(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(30.dp)
          .background(color = Color.LightGray, shape = CircleShape)
          .clip(CircleShape)
          .clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = androidx.compose.material.ripple.rememberRipple(bounded = true, radius = 15.dp)
          ) { 
            onUserAvatarClick?.invoke(post.user) 
          }
      ) {
        Image(
          painter = rememberImagePainter(post.user.image),
          contentDescription = "Profile picture of ${post.user.username}",
          modifier = Modifier.fillMaxSize()
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Text(text = post.user.username, style = MaterialTheme.typography.subtitle2)
    }
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = post.timeStamp.getTimeElapsedText(),
        style = MaterialTheme.typography.caption.copy(fontSize = 10.sp),
        color = Color.Gray
      )
      Spacer(modifier = Modifier.width(8.dp))
      Icon(Filled.MoreVert, "")
    }
  }
}

@Composable
private fun PostContent(
  post: Post,
  onDoubleClick: (Post) -> Unit,
  onHashtagClick: ((String) -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = horizontalPadding)
      .clickable { onDoubleClick.invoke(post) }
  ) {
    // Display post title
    if (post.title.isNotEmpty()) {
      Text(
        text = post.title,
        style = MaterialTheme.typography.h6.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
        color = Color.Black,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp)
      )
    }
    
    // Display post description with inline hashtags
    if (post.text.isNotEmpty()) {
      InlineClickableHashtagText(
        text = post.text,
        style = MaterialTheme.typography.body1,
        defaultColor = Color.Black,
        hashtagColor = Color(0xFF2196F3),
        onHashtagClick = { hashtag ->
          onHashtagClick?.invoke(hashtag)
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp)
      )
    }
  }
}

@Composable
private fun PostFooter(
  post: Post,
  onLikeToggle: (Post) -> Unit,
  onLikeToggleApi: ((Long, Boolean) -> Unit)? = null,
  onCommentClick: ((Post) -> Unit)? = null
) {
  PostFooterIconSection(post, onLikeToggle, onLikeToggleApi, onCommentClick)
}

@Composable
private fun PostFooterIconSection(
  post: Post,
  onLikeToggle: (Post) -> Unit,
  onLikeToggleApi: ((Long, Boolean) -> Unit)? = null,
  onCommentClick: ((Post) -> Unit)? = null
) {

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 5.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.End
  ) {
    AnimLikeButton(post, onLikeToggle, onLikeToggleApi)
    
    Spacer(modifier = Modifier.width(8.dp))
    
    Text(
      text = "${post.likesCount}",
      style = MaterialTheme.typography.caption,
      modifier = Modifier.padding(end = 16.dp)
    )

    PostIconButton(
      onClick = { onCommentClick?.invoke(post) }
    ) {
      Icon(ImageBitmap.imageResource(id = drawable.ic_outlined_comment), "")
    }
    
    Spacer(modifier = Modifier.width(8.dp))
    
    Text(
      text = "${post.commentsCount}",
      style = MaterialTheme.typography.caption
    )
  }
}

@Composable
private fun BestCommentSection(comment: Comment, onUserAvatarClick: ((User) -> Unit)? = null, onHashtagClick: ((String) -> Unit)? = null) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(
        start = horizontalPadding,
        end = horizontalPadding,
        top = 8.dp,
        bottom = 12.dp
      ),
    verticalAlignment = Alignment.Top
  ) {
    // Commenter avatar
    Box(
      modifier = Modifier
        .size(24.dp)
        .background(color = Color.LightGray, shape = CircleShape)
        .clip(CircleShape)
        .clickable(
          interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
          indication = androidx.compose.material.ripple.rememberRipple(bounded = true, radius = 12.dp)
        ) { 
          onUserAvatarClick?.invoke(comment.user) 
        }
    ) {
      Image(
        painter = rememberImagePainter(comment.user.image),
        contentDescription = "Profile picture of ${comment.user.username}",
        modifier = Modifier.fillMaxSize()
      )
    }
    
    Spacer(modifier = Modifier.width(8.dp))
    
    // Comment content
    Column(
      modifier = Modifier.weight(1f)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = comment.user.username,
          style = MaterialTheme.typography.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
          color = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = comment.timeStamp.getTimeElapsedText(),
          style = MaterialTheme.typography.caption.copy(fontSize = 10.sp),
          color = Color.Gray
        )
      }
      
      Spacer(modifier = Modifier.height(2.dp))
      
      InlineClickableHashtagText(
        text = comment.text,
        style = MaterialTheme.typography.caption,
        defaultColor = Color.Black,
        hashtagColor = Color(0xFF2196F3),
        onHashtagClick = { hashtag ->
          // Navigate to search with the hashtag
          onHashtagClick?.invoke(hashtag)
        },
        modifier = Modifier.fillMaxWidth()
      )
      
      Spacer(modifier = Modifier.height(4.dp))
      
      Text(
        text = "${comment.likesCount} likes",
        style = MaterialTheme.typography.caption.copy(fontSize = 10.sp),
        color = Color.Gray
      )
    }
  }
}

private fun Long.getTimeElapsedText(): String {
  val now = System.currentTimeMillis()
  val time = this

  return DateUtils.getRelativeTimeSpanString(
    time, now, 0L, DateUtils.FORMAT_ABBREV_TIME
  )
    .toString()
}

@Composable
private fun HashtagSection(hashtags: List<String>, onHashtagClick: ((String) -> Unit)? = null) {
  if (hashtags.isEmpty()) return
  
  var expanded by remember { mutableStateOf(false) }
  val displayHashtags = if (expanded) hashtags else hashtags.take(2)
  
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = horizontalPadding, vertical = 8.dp)
  ) {
    // Display hashtags as inline text
    InlineClickableHashtagText(
      text = displayHashtags.joinToString(" "),
      style = MaterialTheme.typography.caption,
      defaultColor = Color.Gray,
      hashtagColor = Color(0xFF2196F3),
      onHashtagClick = { hashtag ->
        onHashtagClick?.invoke(hashtag)
      },
      modifier = Modifier.fillMaxWidth()
    )
    
    // Show "more" button if there are more than 2 hashtags
    if (hashtags.size > 2) {
      Text(
        text = if (expanded) "Show less" else "+${hashtags.size - 2} more",
        style = MaterialTheme.typography.caption,
        color = Color(0xFF2196F3), // Blue color for clickable text
        modifier = Modifier
          .clickable { expanded = !expanded }
          .padding(vertical = 4.dp)
      )
    }
  }
}



@Composable
fun InlineClickableHashtagText(
    text: String,
    style: TextStyle,
    defaultColor: Color,
    hashtagColor: Color,
    onHashtagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        val hashtagRegex = Regex("#([a-zA-Z0-9_]+)")
        var lastIndex = 0
        
        hashtagRegex.findAll(text).forEach { matchResult ->
            // Add text before hashtag
            append(text.substring(lastIndex, matchResult.range.first))
            
            // Add hashtag with clickable annotation
            pushStringAnnotation(
                tag = "hashtag",
                annotation = matchResult.value
            )
            withStyle(SpanStyle(color = hashtagColor)) {
                append(matchResult.value)
            }
            pop()
            
            lastIndex = matchResult.range.last + 1
        }
        
        // Add remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
    
    ClickableText(
        text = annotatedString,
        style = style.copy(color = defaultColor),
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "hashtag", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onHashtagClick(annotation.item)
                }
        }
    )
}