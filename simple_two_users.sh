#!/bin/bash

# Simple script to login two users and show DTOs
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
NC='\033[0m'

echo "=========================================="
echo "Simple Two User DTO Test"
echo "=========================================="
echo ""

# Function to authenticate user
authenticate_user() {
    local username=$1
    local password=$2
    
    echo -e "${BLUE}Authenticating user: $username${NC}"
    
    local response=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    
    if echo "$response" | jq -e '.authenticationToken' > /dev/null; then
        local token=$(echo "$response" | jq -r '.authenticationToken')
        local user_id=$(echo "$response" | jq -r '.userId')
        
        echo -e "${GREEN}✓ Authentication successful for $username (ID: $user_id)${NC}"
        echo "$token"
    else
        echo -e "${YELLOW}✗ Authentication failed for $username${NC}"
        echo "$response" | jq -r '.message // "Unknown error"'
        echo ""
    fi
}

# Function to create video call request
create_video_call_request() {
    local username=$1
    local token=$2
    
    echo -e "${BLUE}Creating video call request for $username...${NC}"
    
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
        
        echo -e "${GREEN}✓ Request created for $username${NC}"
        echo -e "${PURPLE}Request ID: $request_id${NC}"
        echo -e "${PURPLE}Status: $status${NC}"
        echo -e "${PURPLE}Position: $position${NC}"
        echo -e "${PURPLE}Total users in queue: $total_users${NC}"
        
        echo -e "${YELLOW}Full DTO Response for $username:${NC}"
        echo "$response" | jq '.'
        echo ""
        
        echo "$request_id"
    else
        echo -e "${YELLOW}✗ Failed to create request for $username${NC}"
        echo "$response" | jq -r '.message // "Unknown error"'
        echo ""
        echo ""
    fi
}

# Function to check request status
check_request_status() {
    local username=$1
    local token=$2
    local request_id=$3
    
    echo -e "${BLUE}Checking status for $username (Request: $request_id)...${NC}"
    
    local response=$(curl -s -X GET "$BASE_URL/api/random-video-calls/status/$request_id" \
        -H "Authorization: Bearer $token")
    
    if echo "$response" | jq -e '.queueStatus' > /dev/null; then
        local status=$(echo "$response" | jq -r '.queueStatus')
        local position=$(echo "$response" | jq -r '.queuePosition // "N/A"')
        local matched_user=$(echo "$response" | jq -r '.matchedUsername // "N/A"')
        local session_id=$(echo "$response" | jq -r '.sessionId // "N/A"')
        local total_users=$(echo "$response" | jq -r '.totalUsersInQueue // "N/A"')
        
        echo -e "${GREEN}Status for $username: $status${NC}"
        echo -e "${PURPLE}Position: $position${NC}"
        echo -e "${PURPLE}Matched user: $matched_user${NC}"
        echo -e "${PURPLE}Session ID: $session_id${NC}"
        echo -e "${PURPLE}Total users: $total_users${NC}"
        
        echo -e "${YELLOW}Full DTO Response for $username:${NC}"
        echo "$response" | jq '.'
        echo ""
    else
        echo -e "${YELLOW}✗ Failed to get status for $username${NC}"
        echo "$response"
        echo ""
    fi
}

# Main execution
echo "Step 1: Authenticating users..."
echo ""

# Authenticate user aa
token_aa=$(authenticate_user "aa" "aa")
echo ""

# Authenticate user bb
token_bb=$(authenticate_user "bb" "bb")
echo ""

if [ -z "$token_aa" ] || [ -z "$token_bb" ]; then
    echo -e "${YELLOW}Failed to authenticate one or both users. Exiting.${NC}"
    exit 1
fi

echo "Step 2: Creating video call requests..."
echo ""

# Create request for user aa
request_aa=$(create_video_call_request "aa" "$token_aa")

# Create request for user bb
request_bb=$(create_video_call_request "bb" "$token_bb")

if [ -z "$request_aa" ] || [ -z "$request_bb" ]; then
    echo -e "${YELLOW}Failed to create one or both requests. Exiting.${NC}"
    exit 1
fi

echo "Step 3: Checking initial status..."
echo ""

# Check status for both users
check_request_status "aa" "$token_aa" "$request_aa"
check_request_status "bb" "$token_bb" "$request_bb"

echo "Step 4: Waiting 10 seconds and checking again..."
echo ""

sleep 10

# Check status again after waiting
check_request_status "aa" "$token_aa" "$request_aa"
check_request_status "bb" "$token_bb" "$request_bb"

echo "Step 5: Final status check after 20 seconds..."
echo ""

sleep 10

# Final status check
check_request_status "aa" "$token_aa" "$request_aa"
check_request_status "bb" "$token_bb" "$request_bb"

echo "=========================================="
echo "Test completed!"
echo "==========================================" 