#!/bin/bash

# ============================================================================
# Docker Build and Push Script
# ============================================================================
# This script builds the Docker image and pushes it to Docker Hub
# Usage: ./docker-build-and-push.sh [version]
# Example: ./docker-build-and-push.sh 0.2.0
# ============================================================================

set -e

# Configuration
DOCKER_USERNAME="flaviomagacho"
IMAGE_NAME="aitosql"
FULL_IMAGE_NAME="${DOCKER_USERNAME}/${IMAGE_NAME}"

# Get version from parameter or default
VERSION="${1:-latest}"
echo "Building version: $VERSION"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "============================================================================"
echo "Docker Build and Push - aiToSql MCP Server"
echo "============================================================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Step 1: Build the image
echo ""
echo "${YELLOW}Step 1: Building Docker image...${NC}"
docker build -t "${FULL_IMAGE_NAME}:${VERSION}" .

# Step 2: Tag as latest if this is a release version
if [[ "$VERSION" != "latest" ]]; then
    echo ""
    echo "${YELLOW}Step 2: Tagging as latest...${NC}"
    docker tag "${FULL_IMAGE_NAME}:${VERSION}" "${FULL_IMAGE_NAME}:latest"
    
    # Also tag major and minor versions
    if [[ "$VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
        MAJOR="${BASH_REMATCH[1]}"
        MINOR="${BASH_REMATCH[2]}"
        
        docker tag "${FULL_IMAGE_NAME}:${VERSION}" "${FULL_IMAGE_NAME}:${MAJOR}"
        docker tag "${FULL_IMAGE_NAME}:${VERSION}" "${FULL_IMAGE_NAME}:${MAJOR}.${MINOR}"
        
        echo "Tagged as: ${MAJOR}, ${MAJOR}.${MINOR}, ${VERSION}, latest"
    fi
fi

# Step 3: Test the image
echo ""
echo "${YELLOW}Step 3: Testing the image...${NC}"
docker run --rm -d \
    --name test-mcp-server \
    -e DB_URL="jdbc:h2:mem:testdb" \
    -e DB_USERNAME="sa" \
    -e DB_PASSWORD="" \
    -e DB_TYPE="H2" \
    -e DB_DRIVER="org.h2.Driver" \
    -p 8082:8080 \
    "${FULL_IMAGE_NAME}:${VERSION}"

echo "Waiting for server to start..."
sleep 15

# Test health endpoint
if curl -f http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo "${GREEN}✓ Health check passed!${NC}"
else
    echo "Error: Health check failed!"
    docker logs test-mcp-server
    docker stop test-mcp-server
    exit 1
fi

# Stop test container
docker stop test-mcp-server

# Step 4: Show image info
echo ""
echo "${YELLOW}Step 4: Image information${NC}"
docker images "${FULL_IMAGE_NAME}" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"

# Step 5: Push to Docker Hub
echo ""
echo "${YELLOW}Step 5: Pushing to Docker Hub...${NC}"
read -p "Do you want to push to Docker Hub? (y/n): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Check if logged in
    if ! docker info | grep -q "Username: ${DOCKER_USERNAME}"; then
        echo "Please login to Docker Hub:"
        docker login
    fi
    
    echo "Pushing ${FULL_IMAGE_NAME}:${VERSION}..."
    docker push "${FULL_IMAGE_NAME}:${VERSION}"
    
    if [[ "$VERSION" != "latest" ]]; then
        echo "Pushing ${FULL_IMAGE_NAME}:latest..."
        docker push "${FULL_IMAGE_NAME}:latest"
        
        # Push major and minor tags
        if [[ "$VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
            MAJOR="${BASH_REMATCH[1]}"
            MINOR="${BASH_REMATCH[2]}"
            
            echo "Pushing ${FULL_IMAGE_NAME}:${MAJOR}..."
            docker push "${FULL_IMAGE_NAME}:${MAJOR}"
            
            echo "Pushing ${FULL_IMAGE_NAME}:${MAJOR}.${MINOR}..."
            docker push "${FULL_IMAGE_NAME}:${MAJOR}.${MINOR}"
        fi
    fi
    
    echo ""
    echo "${GREEN}✓ Successfully pushed to Docker Hub!${NC}"
    echo ""
    echo "Image available at: https://hub.docker.com/r/${DOCKER_USERNAME}/${IMAGE_NAME}"
    echo ""
    echo "Pull with:"
    echo "  docker pull ${FULL_IMAGE_NAME}:${VERSION}"
    echo "  docker pull ${FULL_IMAGE_NAME}:latest"
else
    echo "Skipping push to Docker Hub"
fi

echo ""
echo "============================================================================"
echo "${GREEN}Done!${NC}"
echo "============================================================================"
