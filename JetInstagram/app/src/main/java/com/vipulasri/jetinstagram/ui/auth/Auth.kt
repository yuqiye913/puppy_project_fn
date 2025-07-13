package com.vipulasri.jetinstagram.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.vipulasri.jetinstagram.R
import com.vipulasri.jetinstagram.network.LoginRequest
import com.vipulasri.jetinstagram.network.RetrofitInstance
import com.vipulasri.jetinstagram.network.SignupRequest
import kotlinx.coroutines.launch

@Composable
fun AuthScreen() {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.drawable.star),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 24.dp)
        )
        Text(
            text = if (isLogin) "Welcome Back!" else "Create Account",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = if (isLogin) "Sign in to continue" else "Join our community",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        if (!isLogin) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )
        Button(
            onClick = {
                loading = true
                scope.launch {
                    try {
                        if (isLogin) {
                            val response = RetrofitInstance.api.login(LoginRequest(username, password))
                            if (response.isSuccessful) {
                                val loginResponse = response.body()
                                if (loginResponse != null) {
                                    AuthState.login(loginResponse.authenticationToken, loginResponse.username, loginResponse.userId)
                                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Login failed: No response received", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val response = RetrofitInstance.api.signup(SignupRequest(username, email, password))
                            if (response.isSuccessful) {
                                Toast.makeText(context, response.body()?.message ?: "Signup successful! Please log in.", Toast.LENGTH_SHORT).show()
                                isLogin = true
                            } else {
                                Toast.makeText(context, "Signup failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(text = if (isLogin) "Login" else "Sign Up")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { isLogin = !isLogin }) {
            Text(text = if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login")
        }
    }
} 