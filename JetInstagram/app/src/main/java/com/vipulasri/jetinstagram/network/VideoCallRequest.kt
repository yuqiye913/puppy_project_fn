package com.vipulasri.jetinstagram.network

data class VideoCallRequest(
    val callerId: Long,
    val receiverId: Long,
    val matchId: Long,
    val callType: String = "video",
    val callPurpose: String = "casual",
    val enableVideo: Boolean = true,
    val enableAudio: Boolean = true,
    val videoQuality: String = "high",
    val audioQuality: String = "high",
    val networkType: String? = null,
    val deviceType: String? = null,
    val privacyLevel: String = "matched-only"
) 