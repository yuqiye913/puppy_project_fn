#!/bin/bash

# Clean script to login two users and show DTOs
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
RED='\033[0;31m'
NC='\033[0m'

echo "=========================================="
echo "Clean Two User DTO Test"
echo "=========================================="
echo ""

# Step 1: Authenticate user aa
echo -e "${BLUE}Step 1: Authenticating user 'aa'...${NC}"
response_aa=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"aa","password":"aa"}')

echo "Response for user 'aa':"
echo "$response_aa" | jq '.'
echo ""

# Extract token for user aa
token_aa=$(echo "$response_aa" | jq -r '.authenticationToken // empty')
if [ -z "$token_aa" ]; then
    echo -e "${RED}Failed to get token for user 'aa'${NC}"
    exit 1
fi

# Step 2: Authenticate user bb
echo -e "${BLUE}Step 2: Authenticating user 'bb'...${NC}"
response_bb=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"bb","password":"bb"}')

echo "Response for user 'bb':"
echo "$response_bb" | jq '.'
echo ""

# Extract token for user bb
token_bb=$(echo "$response_bb" | jq -r '.authenticationToken // empty')
if [ -z "$token_bb" ]; then
    echo -e "${RED}Failed to get token for user 'bb'${NC}"
    exit 1
fi

# Step 3: Create video call request for user aa
echo -e "${BLUE}Step 3: Creating video call request for user 'aa'...${NC}"
request_aa_response=$(curl -s -X POST "$BASE_URL/api/random-video-calls/request" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token_aa" \
    -d '{
        "preferredCallType": "video",
        "preferredCallTime": "now",
        "maxWaitTime": 300,
        "preferredGender": "any",
        "preferredAgeRange": "any",
        "preferredLanguage": "any"
    }')

echo "Request DTO for user 'aa':"
echo "$request_aa_response" | jq '.'
echo ""

# Extract request ID for user aa
request_id_aa=$(echo "$request_aa_response" | jq -r '.requestId // empty')

# Step 4: Create video call request for user bb
echo -e "${BLUE}Step 4: Creating video call request for user 'bb'...${NC}"
request_bb_response=$(curl -s -X POST "$BASE_URL/api/random-video-calls/request" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $token_bb" \
    -d '{
        "preferredCallType": "video",
        "preferredCallTime": "now",
        "maxWaitTime": 300,
        "preferredGender": "any",
        "preferredAgeRange": "any",
        "preferredLanguage": "any"
    }')

echo "Request DTO for user 'bb':"
echo "$request_bb_response" | jq '.'
echo ""

# Extract request ID for user bb
request_id_bb=$(echo "$request_bb_response" | jq -r '.requestId // empty')

# Step 5: Check status for both users
echo -e "${BLUE}Step 5: Checking status for both users...${NC}"
echo ""

if [ -n "$request_id_aa" ]; then
    echo -e "${PURPLE}Checking status for user 'aa' (Request ID: $request_id_aa)...${NC}"
    status_aa_response=$(curl -s -X GET "$BASE_URL/api/random-video-calls/status/$request_id_aa" \
        -H "Authorization: Bearer $token_aa")
    
    echo "Status DTO for user 'aa':"
    echo "$status_aa_response" | jq '.'
    echo ""
fi

if [ -n "$request_id_bb" ]; then
    echo -e "${PURPLE}Checking status for user 'bb' (Request ID: $request_id_bb)...${NC}"
    status_bb_response=$(curl -s -X GET "$BASE_URL/api/random-video-calls/status/$request_id_bb" \
        -H "Authorization: Bearer $token_bb")
    
    echo "Status DTO for user 'bb':"
    echo "$status_bb_response" | jq '.'
    echo ""
fi

# Step 6: Wait and check again
echo -e "${BLUE}Step 6: Waiting 10 seconds and checking again...${NC}"
echo ""

sleep 10

if [ -n "$request_id_aa" ]; then
    echo -e "${PURPLE}Updated status for user 'aa'...${NC}"
    status_aa_response2=$(curl -s -X GET "$BASE_URL/api/random-video-calls/status/$request_id_aa" \
        -H "Authorization: Bearer $token_aa")
    
    echo "Updated Status DTO for user 'aa':"
    echo "$status_aa_response2" | jq '.'
    echo ""
fi

if [ -n "$request_id_bb" ]; then
    echo -e "${PURPLE}Updated status for user 'bb'...${NC}"
    status_bb_response2=$(curl -s -X GET "$BASE_URL/api/random-video-calls/status/$request_id_bb" \
        -H "Authorization: Bearer $token_bb")
    
    echo "Updated Status DTO for user 'bb':"
    echo "$status_bb_response2" | jq '.'
    echo ""
fi

echo "=========================================="
echo -e "${GREEN}Test completed!${NC}"
echo "==========================================" 