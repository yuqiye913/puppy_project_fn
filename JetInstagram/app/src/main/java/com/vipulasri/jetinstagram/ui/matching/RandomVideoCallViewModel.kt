package com.vipulasri.jetinstagram.ui.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vipulasri.jetinstagram.data.RandomVideoCallRepository
import com.vipulasri.jetinstagram.network.RandomVideoCallResponse
import com.vipulasri.jetinstagram.network.RandomVideoCallStatistics
import com.vipulasri.jetinstagram.ui.auth.AuthState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class RandomVideoCallViewModel(
    private val repository: RandomVideoCallRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<RandomVideoCallUiState>(RandomVideoCallUiState.Idle)
    val uiState: StateFlow<RandomVideoCallUiState> = _uiState.asStateFlow()
    
    private val _queueStatistics = MutableStateFlow<RandomVideoCallStatistics?>(null)
    val queueStatistics: StateFlow<RandomVideoCallStatistics?> = _queueStatistics.asStateFlow()
    
    private var currentRequestId: String? = null
    private var statusCheckJob: kotlinx.coroutines.Job? = null
    
    fun startRandomVideoCall(userId: Long) {
        viewModelScope.launch {
            // Check if there's already an active request
            if (currentRequestId != null) {
                println("RandomVideoCallViewModel: Already have an active request, not starting a new one")
                return@launch
            }
            
            _uiState.value = RandomVideoCallUiState.Requesting
            
            try {
                val result = repository.requestRandomVideoCall(userId)
                result.fold(
                    onSuccess = { response ->
                        currentRequestId = response.requestId
                        _uiState.value = RandomVideoCallUiState.Waiting(
                            requestId = response.requestId ?: "",
                            queuePosition = response.queuePosition ?: 0,
                            estimatedWaitTime = response.estimatedWaitTime ?: 0,
                            totalUsersInQueue = response.totalUsersInQueue ?: 0
                        )
                        startStatusPolling(response.requestId ?: "")
                    },
                    onFailure = { exception ->
                        _uiState.value = RandomVideoCallUiState.Error(exception.message ?: "Failed to request random video call")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = RandomVideoCallUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    private fun startStatusPolling(requestId: String) {
        statusCheckJob?.cancel()
        statusCheckJob = viewModelScope.launch {
            while (true) {
                delay(2000) // Poll every 2 seconds
                
                try {
                    val result = repository.checkQueueStatus(requestId)
                    result.fold(
                        onSuccess = { response ->
                            when (response.queueStatus) {
                                "connected" -> {
                                    _uiState.value = RandomVideoCallUiState.Connected(
                                        requestId = response.requestId ?: "",
                                        sessionId = response.sessionId ?: "",
                                        roomId = response.roomId ?: "",
                                        peerId = response.peerId ?: "",
                                        signalingData = response.signalingData ?: "",
                                        matchedDisplayName = response.matchedDisplayName ?: "",
                                        matchScore = response.matchScore ?: 0.0
                                    )
                                    return@launch // Stop polling when connected
                                }
                                "matched" -> {
                                    // Handle legacy "matched" status - treat as connected
                                    _uiState.value = RandomVideoCallUiState.Connected(
                                        requestId = response.requestId ?: "",
                                        sessionId = response.sessionId ?: "",
                                        roomId = response.roomId ?: "",
                                        peerId = response.peerId ?: "",
                                        signalingData = response.signalingData ?: "",
                                        matchedDisplayName = response.matchedDisplayName ?: "",
                                        matchScore = response.matchScore ?: 0.0
                                    )
                                    return@launch // Stop polling when matched (treated as connected)
                                }
                                "waiting" -> {
                                    _uiState.value = RandomVideoCallUiState.Waiting(
                                        requestId = response.requestId ?: "",
                                        queuePosition = response.queuePosition ?: 0,
                                        estimatedWaitTime = response.estimatedWaitTime ?: 0,
                                        totalUsersInQueue = response.totalUsersInQueue ?: 0
                                    )
                                }
                                "timeout" -> {
                                    _uiState.value = RandomVideoCallUiState.Timeout
                                    return@launch
                                }
                                "cancelled" -> {
                                    _uiState.value = RandomVideoCallUiState.Cancelled
                                    return@launch
                                }
                                else -> {
                                    // Continue polling
                                }
                            }
                        },
                        onFailure = { exception ->
                            _uiState.value = RandomVideoCallUiState.Error(exception.message ?: "Failed to check status")
                            return@launch
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = RandomVideoCallUiState.Error(e.message ?: "Unknown error occurred")
                    return@launch
                }
            }
        }
    }
    
    fun cancelRandomVideoCall() {
        viewModelScope.launch {
            currentRequestId?.let { requestId ->
                try {
                    val result = repository.cancelRandomVideoCall(requestId)
                    result.fold(
                        onSuccess = {
                            _uiState.value = RandomVideoCallUiState.Cancelled
                            statusCheckJob?.cancel()
                        },
                        onFailure = { exception ->
                            _uiState.value = RandomVideoCallUiState.Error(exception.message ?: "Failed to cancel request")
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = RandomVideoCallUiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }
    
    fun acceptMatchedCall() {
        // This method is no longer needed since matches are automatically accepted
        // Keeping it for backward compatibility but it's not used in the new flow
    }
    
    fun declineMatchedCall(reason: String? = null) {
        viewModelScope.launch {
            currentRequestId?.let { requestId ->
                try {
                    val result = repository.declineRandomVideoCall(requestId, reason)
                    result.fold(
                        onSuccess = {
                            _uiState.value = RandomVideoCallUiState.Declined
                        },
                        onFailure = { exception ->
                            _uiState.value = RandomVideoCallUiState.Error(exception.message ?: "Failed to decline call")
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = RandomVideoCallUiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }
    
    fun endCall() {
        viewModelScope.launch {
            currentRequestId?.let { requestId ->
                try {
                    val result = repository.endRandomVideoCall(requestId)
                    result.fold(
                        onSuccess = {
                            _uiState.value = RandomVideoCallUiState.Ended
                        },
                        onFailure = { exception ->
                            _uiState.value = RandomVideoCallUiState.Error(exception.message ?: "Failed to end call")
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = RandomVideoCallUiState.Error(e.message ?: "Unknown error occurred")
                }
            }
        }
    }
    
    fun loadQueueStatistics() {
        viewModelScope.launch {
            try {
                val result = repository.getQueueStatistics()
                result.fold(
                    onSuccess = { statistics ->
                        _queueStatistics.value = statistics
                    },
                    onFailure = { exception ->
                        // Handle error silently for statistics
                    }
                )
            } catch (e: Exception) {
                // Handle error silently for statistics
            }
        }
    }
    
    fun loadCurrentUserActiveRequest() {
        viewModelScope.launch {
            try {
                val result = repository.getCurrentUserActiveRequest()
                result.fold(
                    onSuccess = { response ->
                        if (response != null) {
                            currentRequestId = response.requestId
                            println("RandomVideoCallViewModel: Retrieved active request ID: ${response.requestId}")
                            
                            // Update UI state based on the response
                            when (response.queueStatus) {
                                "waiting" -> {
                                    _uiState.value = RandomVideoCallUiState.Waiting(
                                        requestId = response.requestId ?: "",
                                        queuePosition = response.queuePosition ?: 0,
                                        estimatedWaitTime = response.estimatedWaitTime ?: 0,
                                        totalUsersInQueue = response.totalUsersInQueue ?: 0
                                    )
                                    // Start polling for this request
                                    response.requestId?.let { startStatusPolling(it) }
                                }
                                "connected" -> {
                                    _uiState.value = RandomVideoCallUiState.Connected(
                                        requestId = response.requestId ?: "",
                                        sessionId = response.sessionId ?: "",
                                        roomId = response.roomId ?: "",
                                        peerId = response.peerId ?: "",
                                        signalingData = response.signalingData ?: "",
                                        matchedDisplayName = response.matchedDisplayName ?: "",
                                        matchScore = response.matchScore ?: 0.0
                                    )
                                }
                                "matched" -> {
                                    // Handle legacy "matched" status - treat as connected
                                    _uiState.value = RandomVideoCallUiState.Connected(
                                        requestId = response.requestId ?: "",
                                        sessionId = response.sessionId ?: "",
                                        roomId = response.roomId ?: "",
                                        peerId = response.peerId ?: "",
                                        signalingData = response.signalingData ?: "",
                                        matchedDisplayName = response.matchedDisplayName ?: "",
                                        matchScore = response.matchScore ?: 0.0
                                    )
                                }
                                else -> {
                                    // For other states, just set the request ID and let the UI handle it
                                    currentRequestId = response.requestId
                                }
                            }
                        } else {
                            println("RandomVideoCallViewModel: No active request found")
                            currentRequestId = null
                        }
                    },
                    onFailure = { exception ->
                        println("RandomVideoCallViewModel: Failed to get active request: ${exception.message}")
                        currentRequestId = null
                    }
                )
            } catch (e: Exception) {
                println("RandomVideoCallViewModel: Exception getting active request: ${e.message}")
                currentRequestId = null
            }
        }
    }
    
    fun reset() {
        statusCheckJob?.cancel()
        currentRequestId = null
        _uiState.value = RandomVideoCallUiState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
        statusCheckJob?.cancel()
    }
}

sealed class RandomVideoCallUiState {
    object Idle : RandomVideoCallUiState()
    object Requesting : RandomVideoCallUiState()
    data class Waiting(
        val requestId: String,
        val queuePosition: Long,
        val estimatedWaitTime: Long,
        val totalUsersInQueue: Long
    ) : RandomVideoCallUiState()

    data class Connected(
        val requestId: String,
        val sessionId: String,
        val roomId: String,
        val peerId: String,
        val signalingData: String,
        val matchedDisplayName: String = "",
        val matchScore: Double = 0.0
    ) : RandomVideoCallUiState()
    object Declined : RandomVideoCallUiState()
    object Timeout : RandomVideoCallUiState()
    object Cancelled : RandomVideoCallUiState()
    object Ended : RandomVideoCallUiState()
    data class Error(val message: String) : RandomVideoCallUiState()
} 