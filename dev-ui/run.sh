#!/bin/bash
# Dev UI - Data Collection Tool for Curated Database
# Crawls from Facebook, checks for duplicate links, saves to humanitarian_logistics_curated.db
# Integrated: Python API + Java Application

cd "$(dirname "$0")" || exit

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Global variables for cleanup
PYTHON_API_PID=""
JAVA_APP_PID=""

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Cleanup function - called on EXIT or Ctrl+C
cleanup() {
    echo ""
    echo "========================================"
    echo "Shutting down..."
    echo "========================================"
    
    # Kill Java app if still running
    if [ -n "$JAVA_APP_PID" ] && kill -0 "$JAVA_APP_PID" 2>/dev/null; then
        print_info "Stopping Java application (PID: $JAVA_APP_PID)..."
        kill "$JAVA_APP_PID" 2>/dev/null
        sleep 1
    fi
    
    # Kill Python API if still running
    if [ -n "$PYTHON_API_PID" ] && kill -0 "$PYTHON_API_PID" 2>/dev/null; then
        print_info "Stopping Python API (PID: $PYTHON_API_PID)..."
        kill "$PYTHON_API_PID" 2>/dev/null
        sleep 1
    fi
    
    # Force kill any remaining processes
    pkill -f "sentiment_api.py" 2>/dev/null || true
    
    print_status "All processes stopped"
}

# Setup trap for cleanup on EXIT or INT (Ctrl+C)
trap cleanup EXIT INT TERM

echo "========================================"
echo "Dev UI - Data Collection Mode"
echo "========================================"
echo "Integrated: Python API + Java Application"
echo ""
echo "Features:"
echo "  🌐 Web Crawler - Crawl from Facebook links and hashtags"
echo "  ✏️  Data Entry - Manually add curated data"
echo "  ✅ Duplicate Link Detection - Prevents adding same link twice"
echo "  📊 Analysis - View data statistics and patterns"
echo "  🔍 Batch Analysis - Analyze posts with Python API"
echo ""
echo "Database: humanitarian_logistics_curated.db"
echo ""

# ============================================
# STEP 1: Start Python API
# ============================================
print_info "STEP 1: Starting Python API for Sentiment & Category Classification"
echo "========================================"

PYTHON_API_PID=""

if command -v python3 &> /dev/null; then
    PYTHON_CMD="python3"
elif command -v python &> /dev/null; then
    PYTHON_CMD="python"
else
    print_error "Python not found. Sentiment analysis will use fallback."
    PYTHON_CMD=""
fi

if [ -n "$PYTHON_CMD" ]; then
    # Kill any existing API process
    pkill -f "sentiment_api.py" 2>/dev/null || true
    sleep 1
    
    PYTHON_API_SCRIPT="./src/main/python/sentiment_api.py"
    if [ -f "$PYTHON_API_SCRIPT" ]; then
        print_info "Starting Python API (with UTF-8 encoding support)..."
        
        # Start API in background
        $PYTHON_CMD "$PYTHON_API_SCRIPT" > .api.log 2>&1 &
        PYTHON_API_PID=$!
        
        # Wait for API to start (up to 10 seconds)
        print_info "Waiting for API initialization..."
        WAIT_COUNT=0
        API_READY=0
        
        while [ $WAIT_COUNT -lt 20 ]; do
            if curl -s http://localhost:5001/health > /dev/null 2>&1; then
                API_READY=1
                break
            fi
            WAIT_COUNT=$((WAIT_COUNT + 1))
            sleep 0.5
        done
        
        if [ $API_READY -eq 1 ]; then
            print_status "Python API ready on http://localhost:5001 (PID: $PYTHON_API_PID)"
            print_info "Sentiment Model: Vietnamese + 100+ languages"
            print_info "Category Model: Keyword-based (instant Vietnamese)"
        else
            print_warning "API taking longer to start - may still be initializing"
            print_info "Check logs: tail -f .api.log"
        fi
    else
        print_error "sentiment_api.py not found at $PYTHON_API_SCRIPT"
    fi
fi

echo ""

# ============================================
# STEP 2: Build Java Application
# ============================================
print_info "STEP 2: Building Java Application"
echo "========================================"

if mvn clean package -DskipTests -q 2>/dev/null; then
    print_status "Build successful!"
else
    print_error "Build failed!"
    exit 1
fi

echo ""

# ============================================
# STEP 3: Start Java Application
# ============================================
print_info "STEP 3: Starting Java Application"
echo "========================================"
echo ""
print_info "Java application starting..."
echo ""

# Start Java application in background and capture its PID
java -jar target/dev-ui.jar "$@" &
JAVA_APP_PID=$!

# Wait for Java app to exit
wait $JAVA_APP_PID 2>/dev/null

# If we get here, Java app has exited
print_info "Java application closed"
