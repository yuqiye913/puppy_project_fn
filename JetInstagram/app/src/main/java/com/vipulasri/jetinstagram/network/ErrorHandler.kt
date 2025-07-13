package com.vipulasri.jetinstagram.network

import com.google.gson.Gson
import retrofit2.Response

object ErrorHandler {
    
    /**
     * Parse error response from API and return user-friendly error message
     */
    fun parseErrorResponse(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        
        if (errorBody != null) {
            try {
                val gson = Gson()
                val errorResponse = gson.fromJson(errorBody, ValidationErrorResponse::class.java)
                
                // Debug: Log the parsed error response
                println("ErrorHandler: Parsed error response - message: '${errorResponse.message}'")
                
                // Display field-specific errors if available
                return if (errorResponse.fieldErrors != null && errorResponse.fieldErrors.isNotEmpty()) {
                    val fieldErrors = errorResponse.fieldErrors.values.joinToString("\n")
                    "Validation errors:\n$fieldErrors"
                } else {
                    errorResponse.message?.takeIf { it.isNotBlank() } ?: "Request failed: ${response.code()}"
                }
            } catch (e: Exception) {
                // Debug: Log the parsing error and raw response
                println("ErrorHandler: Failed to parse error response: ${e.message}")
                println("ErrorHandler: Raw error body: $errorBody")
                return "Request failed: ${response.code()}"
            }
        }
        
        return "Request failed: ${response.code()}"
    }
    
    /**
     * Check if response is a validation error (400 status code)
     */
    fun isValidationError(response: Response<*>): Boolean {
        return response.code() == 400
    }
    
    /**
     * Check if response is an authentication error (401 status code)
     */
    fun isAuthenticationError(response: Response<*>): Boolean {
        return response.code() == 401
    }
} 