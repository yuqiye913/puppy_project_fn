package com.vipulasri.jetinstagram.network

import com.google.gson.annotations.SerializedName

data class RandomVideoCallRequest(
    @SerializedName("userId")
    val userId: Long? = null,
    
    @SerializedName("callType")
    val callType: String = "video",
    
    @SerializedName("callPurpose")
    val callPurpose: String = "casual",
    
    @SerializedName("enableVideo")
    val enableVideo: Boolean = true,
    
    @SerializedName("enableAudio")
    val enableAudio: Boolean = true,
    
    @SerializedName("videoQuality")
    val videoQuality: String = "high",
    
    @SerializedName("audioQuality")
    val audioQuality: String = "high",
    
    @SerializedName("preferredGender")
    val preferredGender: String = "any",
    
    @SerializedName("preferredAgeRange")
    val preferredAgeRange: String = "any",
    
    @SerializedName("preferredLanguage")
    val preferredLanguage: String = "en",
    
    @SerializedName("preferredLocation")
    val preferredLocation: String = "any",
    
    @SerializedName("isPriority")
    val isPriority: Boolean = false,
    
    @SerializedName("queueType")
    val queueType: String = "random"
) 