# Authentication Integration

This document describes how authentication has been integrated into the JetInstagram app.

## Overview

The app now includes a complete authentication flow that allows users to:
- Sign up for new accounts
- Log in with existing credentials
- Access the main app only when authenticated
- Log out from the profile section

## Components

### 1. AuthState (AuthState.kt)
A singleton object that manages the authentication state throughout the app:
- `isLoggedIn`: Boolean indicating if user is authenticated
- `currentToken`: JWT token for API requests
- `currentUsername`: Username of the authenticated user
- `login(token, username)`: Function to log in a user
- `logout()`: Function to log out a user

### 2. AuthScreen (Auth.kt)
The authentication UI that provides:
- Login form with username and password
- Sign up form with username, email, and password
- Toggle between login and signup modes
- Integration with backend API
- Visual feedback with Toast messages

### 3. MainActivity Integration
The MainActivity now uses `AppContent()` composable that:
- Shows AuthScreen when user is not logged in
- Shows MainScreen when user is authenticated
- Automatically switches between screens based on auth state

### 4. Profile Integration
The MyProfile component now includes:
- Logout button in the top app bar
- Uses authenticated username for display

### 5. Upload Integration
The Upload component now:
- Uses the actual JWT token for API requests
- Shows error message if user is not logged in

## Flow

1. **App Launch**: App starts and checks AuthState.isLoggedIn
2. **Not Logged In**: Shows AuthScreen with login/signup options
3. **Login/Signup**: User enters credentials and submits
4. **API Call**: App calls backend authentication API
5. **Success**: AuthState.login() is called, app switches to MainScreen
6. **Main App**: User can access all features with authenticated token
7. **Logout**: User can logout from profile, returns to AuthScreen

## API Integration

The auth screen integrates with the backend API endpoints:
- `POST /api/auth/signup` - For user registration
- `POST /api/auth/login` - For user authentication

## Security Features

- JWT tokens are stored in memory (AuthState)
- Tokens are automatically included in API requests
- Logout clears all authentication data
- Upload functionality requires authentication

## Testing

Run the auth integration tests:
```bash
./gradlew test --tests AuthIntegrationTest
```

## Future Enhancements

- Persistent token storage using SharedPreferences
- Token refresh mechanism
- Biometric authentication
- Password reset functionality
- Social login integration 