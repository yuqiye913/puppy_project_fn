package com.vipulasri.jetinstagram.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.vipulasri.jetinstagram.model.Post
import com.vipulasri.jetinstagram.model.User
import com.vipulasri.jetinstagram.model.names
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
object PostsRepository {

  private val _posts = mutableStateOf<List<Post>>(emptyList())
  val posts: State<List<Post>> = _posts

  private fun populatePosts() {
    val posts = ArrayList<Post>()
    val sampleTexts = arrayOf(
      "Just had the most amazing coffee this morning! ☕️",
      "Working on some exciting new features for our app. Can't wait to share! 💻",
      "Beautiful sunset walk in the park today. Nature is truly healing 🌅",
      "Finished reading an incredible book today. Highly recommend! 📚",
      "Had a great workout session. Feeling energized and ready for the day! 💪",
      "Cooking experiment tonight - trying a new recipe. Wish me luck! 👨‍🍳",
      "Meeting with the team went really well. Great ideas flowing! 🤝",
      "Just discovered this amazing new music artist. Can't stop listening! 🎵",
      "Weekend plans: hiking, reading, and maybe some coding. Perfect! 🏔️",
      "Reflecting on the week and feeling grateful for all the small moments ✨",
      // Additional posts for better search testing
      "Coffee lover here! Always looking for the best brew ☕️",
      "Workout motivation: consistency is key 💪",
      "Nature photography is my passion 📸",
      "Coding late into the night again 💻",
      "Music festival was incredible! 🎵"
    )
    
    val extendedNames = arrayOf(
      "storee", "nianyc", "opioke", "ashoke", "dark_emarlds",
      "bedtan", "shrish", "matdo", "phillsohn", "deitch",
      "coffee_lover", "workout_king", "nature_photographer", "code_master", "music_fan"
    )
    
    (0..14).forEach { index ->
      val post = Post(
          id = index + 1,
          title = "Post ${index + 1}", // Add title
          text = sampleTexts[index],
          user = User(
              id = (index + 2).toLong(), // Start from 2 since 1 is currentUser
              name = extendedNames[index % extendedNames.size],
              username = extendedNames[index % extendedNames.size],
              image = "https://randomuser.me/api/portraits/men/${(index % 10) + 1}.jpg"
          ),
          likesCount = index + 100,
          commentsCount = index + 20,
          timeStamp = System.currentTimeMillis() - (index * 60000),
          bestComment = null // Remove mock comment
      )
      posts.add(post)
    }

    this._posts.value = posts
  }

  init {
    populatePosts()
  }

  suspend fun toggleLike(postId: Int) {
    updateLike(postId, true)
  }

  suspend fun performLike(postId: Int) {
    updateLike(postId, false)
  }

  private suspend fun updateLike(
    postId: Int,
    isToggle: Boolean
  ) {
    withContext(Dispatchers.IO) {
      val posts = _posts.value.toMutableList()
      for ((index, value) in posts.withIndex()) {
        if (value.id == postId) {

          val isLiked = if (isToggle) !value.isLiked else true

          // check if isLiked is same as previous state
          if (isLiked != value.isLiked) {
            val likesCount = if (isLiked) value.likesCount.plus(1) else value.likesCount.minus(1)

            posts[index] = value.copy(isLiked = isLiked, likesCount = likesCount)
          }

          break
        }
      }
      this@PostsRepository._posts.value = posts
    }
  }

}