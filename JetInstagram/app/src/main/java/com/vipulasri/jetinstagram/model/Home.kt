package com.vipulasri.jetinstagram.model

data class Post(
  val id: Int,
  val title: String, // Post title/name
  val text: String, // Post description/content with hashtags
  val image: String? = null, // Made optional for text-only posts
  val user: User,
  val isLiked: Boolean = false,
  val likesCount: Int,
  val commentsCount: Int,
  val timeStamp: Long,
  val bestComment: Comment? = null,
  val hashtags: List<String> = emptyList() // Subreddit names as hashtags
)

data class Comment(
  val id: Int,
  val text: String,
  val user: User,
  val likesCount: Int,
  val timeStamp: Long,
  val replyCount: Int = 0,
  val parentCommentId: Int? = null,
  val replies: List<Comment>? = null,
  val repliesLoaded: Boolean = false,
  val loadingReplies: Boolean = false,
  val isLiked: Boolean = false
)

data class Story(
  val image: String,
  val name: String,
  val isSeen: Boolean = false
)

val names = arrayOf(
    "storee",
    "nianyc",
    "opioke",
    "ashoke",
    "dark_emarlds",
    "bedtan",
    "shrish",
    "matdo",
    "phillsohn",
    "deitch"
)