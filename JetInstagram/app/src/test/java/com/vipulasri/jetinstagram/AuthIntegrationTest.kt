package com.vipulasri.jetinstagram

import com.vipulasri.jetinstagram.ui.auth.AuthState
import org.junit.Test
import org.junit.Assert.*

class AuthIntegrationTest {
    
    @Test
    fun testAuthStateLogin() {
        // Reset state
        AuthState.logout()
        
        // Test initial state
        assertFalse(AuthState.isLoggedIn)
        assertNull(AuthState.currentToken)
        assertNull(AuthState.currentUsername)
        
        // Test login
        val testToken = "test_jwt_token"
        val testUsername = "testuser"
        AuthState.login(testToken, testUsername)
        
        // Verify login state
        assertTrue(AuthState.isLoggedIn)
        assertEquals(testToken, AuthState.currentToken)
        assertEquals(testUsername, AuthState.currentUsername)
    }
    
    @Test
    fun testAuthStateLogout() {
        // Setup logged in state
        AuthState.login("test_token", "testuser")
        assertTrue(AuthState.isLoggedIn)
        
        // Test logout
        AuthState.logout()
        
        // Verify logout state
        assertFalse(AuthState.isLoggedIn)
        assertNull(AuthState.currentToken)
        assertNull(AuthState.currentUsername)
    }
} 