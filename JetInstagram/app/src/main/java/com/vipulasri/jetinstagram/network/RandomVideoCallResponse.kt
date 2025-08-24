package com.vipulasri.jetinstagram.network

import com.google.gson.annotations.SerializedName

data class RandomVideoCallResponse(
    @SerializedName("requestId")
    val requestId: String? = null,
    
    @SerializedName("queueStatus")
    val queueStatus: String? = null,
    
    @SerializedName("queuePosition")
    val queuePosition: Long? = null,
    
    @SerializedName("estimatedWaitTime")
    val estimatedWaitTime: Long? = null,
    
    @SerializedName("totalUsersInQueue")
    val totalUsersInQueue: Long? = null,
    
    @SerializedName("averageWaitTime")
    val averageWaitTime: Long? = null,
    
    @SerializedName("sessionId")
    val sessionId: String? = null,
    
    @SerializedName("matchedUserId")
    val matchedUserId: Long? = null,
    
    @SerializedName("matchedUsername")
    val matchedUsername: String? = null,
    
    @SerializedName("matchedDisplayName")
    val matchedDisplayName: String? = null,
    
    @SerializedName("matchedProfilePicture")
    val matchedProfilePicture: String? = null,
    
    @SerializedName("callType")
    val callType: String? = null,
    
    @SerializedName("callPurpose")
    val callPurpose: String? = null,
    
    @SerializedName("videoEnabled")
    val videoEnabled: Boolean = true,
    
    @SerializedName("audioEnabled")
    val audioEnabled: Boolean = true,
    
    @SerializedName("videoQuality")
    val videoQuality: String? = null,
    
    @SerializedName("audioQuality")
    val audioQuality: String? = null,
    
    @SerializedName("roomId")
    val roomId: String? = null,
    
    @SerializedName("peerId")
    val peerId: String? = null,
    
    @SerializedName("signalingData")
    val signalingData: String? = null,
    
    @SerializedName("offerSdp")
    val offerSdp: String? = null,
    
    @SerializedName("answerSdp")
    val answerSdp: String? = null,
    
    @SerializedName("iceCandidates")
    val iceCandidates: String? = null,
    
    @SerializedName("matchScore")
    val matchScore: Double? = null,
    
    @SerializedName("matchReason")
    val matchReason: String? = null
) 