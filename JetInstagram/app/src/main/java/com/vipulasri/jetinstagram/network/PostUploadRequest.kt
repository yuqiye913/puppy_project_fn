package com.vipulasri.jetinstagram.network

data class PostUploadRequest(
    val postName: String,
    val url: String? = null,
    val description: String,
    val subredditNames: List<String>? = null,
    val location: String? = null,
    val hashtags: List<String>? = null,
    val friendsOnly: Boolean? = null
) 