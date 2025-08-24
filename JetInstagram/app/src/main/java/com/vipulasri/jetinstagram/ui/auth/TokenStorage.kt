package com.vipulasri.jetinstagram.ui.auth

import android.content.Context
import android.content.SharedPreferences

object TokenStorage {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USERNAME = "username"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    private lateinit var prefs: SharedPreferences
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveAuthData(token: String, refreshToken: String?, username: String, userId: Long) {
        println("TokenStorage: saveAuthData called with userId: $userId")
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USERNAME, username)
            putLong(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
        }.apply()
        println("TokenStorage: Auth data saved successfully")
    }
    
    fun getAuthToken(): String? = prefs.getString(KEY_TOKEN, null)
    
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    
    fun getUserId(): Long {
        val userId = prefs.getLong(KEY_USER_ID, -1L)
        println("TokenStorage: getUserId() called, returning: $userId")
        return userId
    }
    
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    
    fun clearAuthData() {
        prefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USERNAME)
            remove(KEY_USER_ID)
            putBoolean(KEY_IS_LOGGED_IN, false)
        }.apply()
    }
    
    fun updateToken(newToken: String) {
        prefs.edit().putString(KEY_TOKEN, newToken).apply()
    }
    
    fun updateRefreshToken(newRefreshToken: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, newRefreshToken).apply()
    }
} 