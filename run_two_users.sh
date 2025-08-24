#!/bin/bash

# Script to run two client simulators in parallel
# This will simulate two users (aa and bb) to test the matching system

BASE_URL="http://localhost:8080"
LOG_FILE="two_users_simulation.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
log_message() {
    local color=$1
    local user=$2
    local message=$3
    local timestamp=$(date '+%H:%M:%S')
    echo -e "${color}[${timestamp}] [${user}] ${message}${NC}"
    echo "[${timestamp}] [${user}] ${message}" >> "$LOG_FILE"
}

# Function to authenticate user
authenticate_user() {
    local username=$1
    local password=$2
    
    log_message $BLUE "$username" "Authenticating..."
    
    local response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    
    if echo "$response" | jq -e '.authenticationToken' > /dev/null; then
        local token=$(echo "$response" | jq -r '.authenticationToken')
        local user_id=$(echo "$response" | jq -r '.userId')
        
        log_message $GREEN "$username" "✓ Authentication successful (ID: $user_id)"
        echo "$token"
    else
        log_message $RED "$username" "✗ Authentication failed"
        echo ""
    fi
}

# Function to create video call request
create_video_call_request() {
    local username=$1
    local token=$2
    
    log_message $BLUE "$username" "Creating video call request..."
    
    local response=$(curl -s -X POST "$BASE_URL/api/random-video-calls/request" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $token" \
        -d '{
            "preferredCallType": "video",
            "preferredCallTime": "now",
            "maxWaitTime": 300,
            "preferredGender": "any",
            "preferredAgeRange": "any",
            "preferredLanguage": "any"
        }')
    
    if echo "$response" | jq -e '.requestId' > /dev/null; then
        local request_id=$(echo "$response" | jq -r '.requestId')
        local status=$(echo "$response" | jq -r '.queueStatus')
        local position=$(echo "$response" | jq -r '.queuePosition')
        local total_users=$(echo "$response" | jq -r '.totalUsersInQueue')
        
        log_message $GREEN "$username" "✓ Request created (ID: $request_id, Position: $position, Total: $total_users)"
        echo "$request_id"
    else
        log_message $RED "$username" "✗ Failed to create request"
        echo ""
    fi
}

# Function to monitor request status
monitor_request() {
    local username=$1
    local token=$2
    local request_id=$3
    local max_attempts=${4:-150} # 5 minutes default
    local attempt=0
    
    log_message $BLUE "$username" "Starting to monitor request: $request_id"
    log_message $YELLOW "$username" "Maximum wait time: $((max_attempts * 2)) seconds"
    
    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))
        
        # Get current status
        local response=$(curl -s -X GET "$BASE_URL/api/random-video-calls/status/$request_id" \
            -H "Authorization: Bearer $token")
        
        if echo "$response" | jq -e '.queueStatus' > /dev/null; then
            local status=$(echo "$response" | jq -r '.queueStatus')
            local position=$(echo "$response" | jq -r '.queuePosition // "N/A"')
            local matched_user=$(echo "$response" | jq -r '.matchedUsername // "N/A"')
            local session_id=$(echo "$response" | jq -r '.sessionId // "N/A"')
            local total_users=$(echo "$response" | jq -r '.totalUsersInQueue // "N/A"')
            local match_score=$(echo "$response" | jq -r '.matchScore // "N/A"')
            
            # Log the DTO response
            log_message $PURPLE "$username" "DTO: status=$status, position=$position, matched=$matched_user, session=$session_id"
            
            case $status in
                "waiting")
                    log_message $YELLOW "$username" "⏳ Waiting... Position: $position, Total: $total_users"
                    ;;
                "matched")
                    log_message $GREEN "$username" "🎉 MATCHED! with user: $matched_user"
                    log_message $GREEN "$username" "Session ID: $session_id, Score: $match_score"
                    log_message $GREEN "$username" "Match found after $((attempt * 2)) seconds"
                    return 0
                    ;;
                "connected")
                    log_message $GREEN "$username" "📞 CONNECTED! Video call active"
                    log_message $GREEN "$username" "Session ID: $session_id"
                    return 0
                    ;;
                "timeout")
                    log_message $RED "$username" "⏰ TIMEOUT! Request timed out"
                    return 1
                    ;;
                "cancelled")
                    log_message $RED "$username" "❌ CANCELLED! Request cancelled"
                    return 1
                    ;;
                *)
                    log_message $PURPLE "$username" "❓ Unknown status: $status"
                    ;;
            esac
        else
            log_message $RED "$username" "✗ Failed to get status response"
            echo "$response"
            return 1
        fi
        
        sleep 2
    done
    
    log_message $RED "$username" "⏰ TIMEOUT! Maximum attempts reached"
    return 1
}

# Function to check system status
check_system_status() {
    log_message $BLUE "SYSTEM" "Checking matching system status..."
    
    # Use user aa's token for admin check
    local token_aa=$(authenticate_user "aa" "aa")
    if [ -n "$token_aa" ]; then
        local response=$(curl -s -X GET "$BASE_URL/api/admin/matching/system-status" \
            -H "Authorization: Bearer $token_aa")
        
        if echo "$response" | jq -e '.matchingEnabled' > /dev/null; then
            local enabled=$(echo "$response" | jq -r '.matchingEnabled')
            local total_users=$(echo "$response" | jq -r '.totalUsersInQueue')
            local status=$(echo "$response" | jq -r '.status')
            local matches_today=$(echo "$response" | jq -r '.matchesToday')
            
            log_message $GREEN "SYSTEM" "✓ Matching system: $status"
            log_message $CYAN "SYSTEM" "Users in queue: $total_users"
            log_message $CYAN "SYSTEM" "Matching enabled: $enabled"
            log_message $CYAN "SYSTEM" "Matches today: $matches_today"
        else
            log_message $RED "SYSTEM" "✗ Failed to get system status"
        fi
    fi
}

# Main function
main() {
    echo "=========================================="
    echo "Two User Matching Simulation"
    echo "=========================================="
    echo ""
    
    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        echo "Error: jq is not installed. Please install jq to parse JSON responses."
        exit 1
    fi
    
    # Check system status first
    check_system_status
    echo ""
    
    log_message $BLUE "MAIN" "Starting two user simulation..."
    
    # Authenticate both users
    log_message $BLUE "MAIN" "Authenticating users..."
    local token_aa=$(authenticate_user "aa" "aa")
    local token_bb=$(authenticate_user "bb" "bb")
    
    if [ -z "$token_aa" ] || [ -z "$token_bb" ]; then
        log_message $RED "MAIN" "Failed to authenticate users. Exiting."
        exit 1
    fi
    
    echo ""
    log_message $BLUE "MAIN" "Creating video call requests..."
    
    # Create video call requests
    local request_aa=$(create_video_call_request "aa" "$token_aa")
    local request_bb=$(create_video_call_request "bb" "$token_bb")
    
    if [ -z "$request_aa" ] || [ -z "$request_bb" ]; then
        log_message $RED "MAIN" "Failed to create requests. Exiting."
        exit 1
    fi
    
    echo ""
    log_message $BLUE "MAIN" "Both users are now in queue. Starting monitoring..."
    log_message $YELLOW "MAIN" "Press Ctrl+C to stop monitoring"
    echo ""
    
    # Monitor both requests in parallel
    monitor_request "aa" "$token_aa" "$request_aa" &
    local pid_aa=$!
    
    monitor_request "bb" "$token_bb" "$request_bb" &
    local pid_bb=$!
    
    # Wait for both processes
    wait $pid_aa
    local result_aa=$?
    
    wait $pid_bb
    local result_bb=$?
    
    echo ""
    log_message $BLUE "MAIN" "Simulation completed!"
    
    if [ $result_aa -eq 0 ] && [ $result_bb -eq 0 ]; then
        log_message $GREEN "MAIN" "🎉 Both users were successfully matched!"
    else
        log_message $RED "MAIN" "❌ One or both users failed to match"
    fi
    
    echo ""
    log_message $BLUE "MAIN" "Check the log file: $LOG_FILE"
}

# Handle Ctrl+C gracefully
trap 'echo ""; log_message $YELLOW "MAIN" "Received interrupt signal. Stopping..."; exit 0' INT

# Run main function
main 