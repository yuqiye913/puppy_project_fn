package com.vipulasri.jetinstagram.ui.videocall

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.*

@Composable
fun WebRTCVideoView(
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false
) {
    val context = LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            val surfaceViewRenderer = SurfaceViewRenderer(ctx)
            surfaceViewRenderer.init(
                EglBase.create().eglBaseContext,
                null
            )
            surfaceViewRenderer.setMirror(isMirrored)
            surfaceViewRenderer
        },
        modifier = modifier,
        update = { surfaceViewRenderer ->
            // For now, just log that we would add the video track
            // The libjingle library might have different method names
            println("WebRTCVideoView: Would add video track to surface")
        }
    )
}

@Composable
fun LocalVideoView(
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier
) {
    WebRTCVideoView(
        videoTrack = videoTrack,
        modifier = modifier,
        isMirrored = true // Mirror local video
    )
}

@Composable
fun RemoteVideoView(
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier
) {
    WebRTCVideoView(
        videoTrack = videoTrack,
        modifier = modifier,
        isMirrored = false // Don't mirror remote video
    )
} 