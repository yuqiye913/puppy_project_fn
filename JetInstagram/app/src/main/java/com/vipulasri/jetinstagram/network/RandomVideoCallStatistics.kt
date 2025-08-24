package com.vipulasri.jetinstagram.network

import com.google.gson.annotations.SerializedName

data class RandomVideoCallStatistics(
    @SerializedName("totalUsersInQueue")
    val totalUsersInQueue: Long = 0,
    
    @SerializedName("priorityUsersInQueue")
    val priorityUsersInQueue: Long = 0,
    
    @SerializedName("averageWaitTime")
    val averageWaitTime: Long = 0,
    
    @SerializedName("estimatedWaitTime")
    val estimatedWaitTime: Long = 0,
    
    @SerializedName("totalMatchesToday")
    val totalMatchesToday: Long = 0,
    
    @SerializedName("successfulCallsToday")
    val successfulCallsToday: Long = 0,
    
    @SerializedName("failedCallsToday")
    val failedCallsToday: Long = 0
) 