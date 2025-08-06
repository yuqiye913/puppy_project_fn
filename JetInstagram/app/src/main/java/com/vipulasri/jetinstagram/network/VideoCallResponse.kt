package com.vipulasri.jetinstagram.network

data class VideoCallResponse(
    val sessionId: String?,
    val responseStatus: String?,
    val responseMessage: String?,
    val callerId: Long?,
    val receiverId: Long?,
    val matchId: Long?,
    val callType: String?,
    val callStatus: String?,
    val roomId: String?,
    val peerId: String?,
    val signalingData: String?,
    val offerSdp: String?,
    val answerSdp: String?,
    val iceCandidates: String?,
    val videoEnabled: Boolean = true,
    val audioEnabled: Boolean = true,
    val videoQuality: String?,
    val audioQuality: String?,
    val networkType: String?,
    val deviceType: String?,
    val connectionQuality: String?,
    val privacyLevel: String?,
    val encrypted: Boolean = true,
    val encryptionType: String?
) 