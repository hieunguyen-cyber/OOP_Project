#!/bin/bash
# Run Humanitarian Logistics Application

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$APP_DIR" || exit 1

echo "============================================"
echo "Humanitarian Logistics Analysis System"
echo "============================================"
echo ""

# Check if installer has completed (marked by .installed file in parent directory)
PARENT_DIR="$(dirname "$APP_DIR")"
INSTALL_MARKER="$PARENT_DIR/.installed"

if [ ! -f "$INSTALL_MARKER" ]; then
    echo "First-time setup detected. Running installer..."
    echo ""
    
    if [ ! -f "$PARENT_DIR/install.sh" ]; then
        echo "Error: install.sh not found!"
        exit 1
    fi
    
    bash "$PARENT_DIR/install.sh"
    exit $?
fi

echo "Building application..."
mvn clean compile package -DskipTests -q

if [ $? -eq 0 ]; then
    echo "✓ Build successful!"
    echo ""
    echo "Starting application with Java Swing GUI..."
    echo "The application window will appear on your screen."
    echo ""
    echo "Features:"
    echo "  - Data Collection Panel: Add posts and comments manually"
    echo "  - Use Our Database: Load 31 curated posts"
    echo "  - Problem 1 Analysis: Public satisfaction per relief category"
    echo "  - Problem 2 Analysis: Temporal sentiment tracking over time"
    echo "  - Multiple visualization charts"
    echo ""
    echo "Starting in 2 seconds..."
    sleep 2
    
    mvn exec:java -Dexec.mainClass="com.humanitarian.logistics.HumanitarianLogisticsApp"
else
    echo "✗ Build failed!"
    exit 1
fi
