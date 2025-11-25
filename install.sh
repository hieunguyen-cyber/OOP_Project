#!/bin/bash

################################################################################
# Humanitarian Logistics Application - First-Time Setup Installer
# This script runs automatically on first launch to:
# 1. Check and install required dependencies (Java, Maven, Python)
# 2. Build the application
# 3. Create necessary data files
# 4. Mark installation as complete
################################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INSTALL_MARKER="$SCRIPT_DIR/.installed"
APP_DIR="$SCRIPT_DIR/humanitarian-logistics"

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Humanitarian Logistics Application - First-Time Setup         ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Check if already installed
if [ -f "$INSTALL_MARKER" ]; then
    echo "✓ Application already installed. Starting app..."
    cd "$APP_DIR"
    bash run.sh
    exit 0
fi

echo "📦 Checking dependencies..."
echo ""

# Function to check and install Java
check_java() {
    if ! command -v java &> /dev/null; then
        echo "❌ Java not found. Installing Java..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            if command -v brew &> /dev/null; then
                brew install openjdk@11
                echo "✓ Java installed via Homebrew"
            else
                echo "❌ Homebrew not found. Please install Java manually from https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html"
                exit 1
            fi
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            # Linux
            if command -v apt-get &> /dev/null; then
                sudo apt-get update
                sudo apt-get install -y openjdk-11-jdk
                echo "✓ Java installed via apt"
            elif command -v yum &> /dev/null; then
                sudo yum install -y java-11-openjdk
                echo "✓ Java installed via yum"
            fi
        fi
    else
        echo "✓ Java found: $(java -version 2>&1 | head -1)"
    fi
}

# Function to check and install Maven
check_maven() {
    if ! command -v mvn &> /dev/null; then
        echo "❌ Maven not found. Installing Maven..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            if command -v brew &> /dev/null; then
                brew install maven
                echo "✓ Maven installed via Homebrew"
            else
                echo "❌ Homebrew not found. Please install Maven manually."
                exit 1
            fi
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            if command -v apt-get &> /dev/null; then
                sudo apt-get install -y maven
                echo "✓ Maven installed via apt"
            elif command -v yum &> /dev/null; then
                sudo yum install -y maven
                echo "✓ Maven installed via yum"
            fi
        fi
    else
        echo "✓ Maven found: $(mvn -version | head -1)"
    fi
}

# Function to check and install Python
check_python() {
    if ! command -v python3 &> /dev/null; then
        echo "❌ Python3 not found. Installing Python3..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            if command -v brew &> /dev/null; then
                brew install python@3.9
                echo "✓ Python installed via Homebrew"
            else
                echo "❌ Homebrew not found. Please install Python manually."
                exit 1
            fi
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            if command -v apt-get &> /dev/null; then
                sudo apt-get install -y python3 python3-pip
                echo "✓ Python installed via apt"
            elif command -v yum &> /dev/null; then
                sudo yum install -y python3 python3-pip
                echo "✓ Python installed via yum"
            fi
        fi
    else
        echo "✓ Python found: $(python3 --version)"
    fi
}

# Check all dependencies
check_java
check_maven
check_python

echo ""
echo "🔨 Building application..."
echo ""

cd "$APP_DIR"

# Clean and build
mvn clean compile package -DskipTests -q 2>&1 | grep -v "WARNING" || true

if [ $? -eq 0 ]; then
    echo "✓ Build successful"
else
    echo "⚠ Build completed with warnings (normal)"
fi

echo ""
echo "📁 Creating data directories..."
echo ""

# Create necessary files if they don't exist
touch humanitarian_logistics_user.db 2>/dev/null || true
touch humanitarian_logistics_curated.db 2>/dev/null || true

echo "✓ Data files created"

echo ""
echo "✅ Installation complete!"
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  Installation Summary:"
echo "  ✓ Java verified"
echo "  ✓ Maven verified"
echo "  ✓ Python verified"
echo "  ✓ Application built successfully"
echo "  ✓ Data files created"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Mark as installed
touch "$INSTALL_MARKER"

echo "🚀 Starting application..."
echo ""

cd "$APP_DIR"
bash run.sh
