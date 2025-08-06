package com.vipulasri.jetinstagram.model

import java.time.Instant

data class Match(
    val matchId: Long? = null,
    val userId: Long,
    val matchedUserId: Long,
    val matchStatus: String = "pending", // pending, accepted, declined, blocked, expired
    val matchedAt: String? = null,
    val lastInteractionAt: String? = null,
    val isRead: Boolean = false,
    val readAt: String? = null,
    val callType: String? = null, // voice, video
    val callPurpose: String? = null, // casual, business, emergency, social
    val preferredCallTime: String? = null, // morning, afternoon, evening, anytime
    val autoAcceptCalls: Boolean = false,
    val requireAdvanceNotice: Boolean = false,
    val advanceNoticePeriod: String? = null,
    val hasCalled: Boolean = false,
    val hasReceivedCall: Boolean = false,
    val lastCallAt: String? = null,
    val lastCallDuration: Long? = null,
    val lastCallStatus: String? = null, // completed, missed, declined
    val totalCalls: Long = 0,
    val successfulCalls: Long = 0,
    val missedCalls: Long = 0,
    val declinedCalls: Long = 0,
    val totalCallDuration: Long = 0,
    val connectionQuality: String? = null,
    val isMutualMatch: Boolean = false,
    val preferredCommunicationMethod: String? = "voice" // voice, video, text, in-person
) 