package com.vipulasri.jetinstagram.ui.videocall

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberVideoCallPermissions(): VideoCallPermissions {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasMicrophonePermission by remember { mutableStateOf(false) }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicrophonePermission = isGranted
    }
    
    val requestPermissions = {
        // Check current permissions
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        hasMicrophonePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        // Request permissions if not granted
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        
        if (!hasMicrophonePermission) {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    
    return remember {
        VideoCallPermissions(
            hasCameraPermission = hasCameraPermission,
            hasMicrophonePermission = hasMicrophonePermission,
            requestPermissions = requestPermissions
        )
    }
}

data class VideoCallPermissions(
    val hasCameraPermission: Boolean,
    val hasMicrophonePermission: Boolean,
    val requestPermissions: () -> Unit
) {
    val allPermissionsGranted: Boolean
        get() = hasCameraPermission && hasMicrophonePermission
} 