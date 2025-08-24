package com.vipulasri.jetinstagram.videocall

import android.util.Log
import com.vipulasri.jetinstagram.network.ApiService
import com.vipulasri.jetinstagram.network.WebRTCIceCandidateRequest
import com.vipulasri.jetinstagram.network.WebRTCSessionDescriptionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class SignalingManager(
    private val apiService: ApiService,
    private val authToken: String
) {
    private var sessionId: String? = null
    private var roomId: String? = null
    private var isPolling = false
    private var lastUpdate = 0L
    
    // Callbacks
    var onOfferReceived: ((SessionDescription) -> Unit)? = null
    var onAnswerReceived: ((SessionDescription) -> Unit)? = null
    var onIceCandidateReceived: ((IceCandidate) -> Unit)? = null
    var onSignalingError: ((String) -> Unit)? = null
    
    fun setSessionInfo(sessionId: String, roomId: String) {
        this.sessionId = sessionId
        this.roomId = roomId
        Log.d(TAG, "Signaling session set: $sessionId, room: $roomId")
    }
    
    fun sendOffer(offer: SessionDescription) {
        sessionId?.let { sessionId ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = WebRTCSessionDescriptionRequest(
                        sessionId = sessionId,
                        type = offer.type.name.lowercase(),
                        sdp = offer.description
                    )
                    
                    val response = apiService.updateWebRTCSessionDescription(authToken, sessionId, request)
                    if (response.isSuccessful) {
                        Log.d(TAG, "Offer sent successfully: ${offer.description.take(100)}...")
                    } else {
                        Log.e(TAG, "Failed to send offer: ${response.code()}")
                        onSignalingError?.invoke("Failed to send offer: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending offer", e)
                    onSignalingError?.invoke("Error sending offer: ${e.message}")
                }
            }
        }
    }
    
    fun sendAnswer(answer: SessionDescription) {
        sessionId?.let { sessionId ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = WebRTCSessionDescriptionRequest(
                        sessionId = sessionId,
                        type = answer.type.name.lowercase(),
                        sdp = answer.description
                    )
                    
                    val response = apiService.updateWebRTCSessionDescription(authToken, sessionId, request)
                    if (response.isSuccessful) {
                        Log.d(TAG, "Answer sent successfully: ${answer.description.take(100)}...")
                    } else {
                        Log.e(TAG, "Failed to send answer: ${response.code()}")
                        onSignalingError?.invoke("Failed to send answer: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending answer", e)
                    onSignalingError?.invoke("Error sending answer: ${e.message}")
                }
            }
        }
    }
    
    fun sendIceCandidate(candidate: IceCandidate) {
        sessionId?.let { sessionId ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = WebRTCIceCandidateRequest(
                        sessionId = sessionId,
                        candidate = candidate.sdp,
                        sdpMid = candidate.sdpMid,
                        sdpMLineIndex = candidate.sdpMLineIndex
                    )
                    
                    val response = apiService.addWebRTCCandidate(authToken, sessionId, request)
                    if (response.isSuccessful) {
                        Log.d(TAG, "ICE candidate sent successfully: ${candidate.sdp.take(100)}...")
                    } else {
                        Log.e(TAG, "Failed to send ICE candidate: ${response.code()}")
                        onSignalingError?.invoke("Failed to send ICE candidate: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending ICE candidate", e)
                    onSignalingError?.invoke("Error sending ICE candidate: ${e.message}")
                }
            }
        }
    }
    
    fun startPolling() {
        if (isPolling) return
        
        isPolling = true
        CoroutineScope(Dispatchers.IO).launch {
            while (isPolling && sessionId != null) {
                try {
                    val response = apiService.pollWebRTCUpdates(authToken, sessionId!!, lastUpdate)
                    if (response.isSuccessful) {
                        val signalingData = response.body()
                        if (signalingData != null) {
                            handleSignalingUpdate(signalingData)
                            lastUpdate = signalingData.timestamp ?: lastUpdate
                        }
                    } else {
                        Log.e(TAG, "Polling failed: ${response.code()}")
                        if (response.code() == 404) {
                            // Session not found, stop polling
                            stopPolling()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during polling", e)
                }
                
                delay(2000) // Poll every 2 seconds
            }
        }
        Log.d(TAG, "Started signaling polling")
    }
    
    fun stopPolling() {
        isPolling = false
        Log.d(TAG, "Stopped signaling polling")
    }
    
    private fun handleSignalingUpdate(signalingData: com.vipulasri.jetinstagram.network.WebRTCSignalingResponse) {
        try {
            // Handle session description updates
            signalingData.localDescription?.let { sdp ->
                // Determine if it's an offer or answer based on the SDP content
                val sessionDescription = when {
                    sdp.contains("a=sendonly") -> SessionDescription(SessionDescription.Type.OFFER, sdp)
                    sdp.contains("a=recvonly") -> SessionDescription(SessionDescription.Type.ANSWER, sdp)
                    else -> SessionDescription(SessionDescription.Type.OFFER, sdp) // Default to offer
                }
                
                when (sessionDescription.type) {
                    SessionDescription.Type.OFFER -> onOfferReceived?.invoke(sessionDescription)
                    SessionDescription.Type.ANSWER -> onAnswerReceived?.invoke(sessionDescription)
                    else -> Log.d(TAG, "Received session description of type: ${sessionDescription.type}")
                }
            }
            
            // Handle ICE candidate updates
            signalingData.iceCandidates?.forEach { candidateString ->
                try {
                    // Parse ICE candidate string
                    val candidate = parseIceCandidate(candidateString)
                    onIceCandidateReceived?.invoke(candidate)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing ICE candidate: $candidateString", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling signaling update", e)
        }
    }
    
    private fun parseIceCandidate(candidateString: String): IceCandidate {
        // Simple ICE candidate parsing - in a real implementation, you'd want more robust parsing
        val parts = candidateString.split(" ")
        if (parts.size >= 8) {
            val candidate = parts[0]
            val sdpMid = parts[1]
            val sdpMLineIndex = parts[2].toInt()
            
            return IceCandidate(sdpMid, sdpMLineIndex, candidate)
        } else {
            // Fallback for malformed candidates
            return IceCandidate("0", 0, candidateString)
        }
    }
    
    fun createSession() {
        sessionId?.let { sessionId ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.createWebRTCSession(authToken, sessionId)
                    if (response.isSuccessful) {
                        Log.d(TAG, "WebRTC session created successfully")
                    } else {
                        Log.e(TAG, "Failed to create WebRTC session: ${response.code()}")
                        onSignalingError?.invoke("Failed to create WebRTC session: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating WebRTC session", e)
                    onSignalingError?.invoke("Error creating WebRTC session: ${e.message}")
                }
            }
        }
    }
    
    fun closeSession() {
        sessionId?.let { sessionId ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    stopPolling()
                    val response = apiService.closeWebRTCSession(authToken, sessionId)
                    if (response.isSuccessful) {
                        Log.d(TAG, "WebRTC session closed successfully")
                    } else {
                        Log.e(TAG, "Failed to close WebRTC session: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing WebRTC session", e)
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "SignalingManager"
    }
} 