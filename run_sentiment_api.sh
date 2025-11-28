#!/bin/bash

# Sentiment Analysis API Server with Vietnamese Support
# This runs the Python Flask API that Dev UI connects to

cd "$(dirname "$0")" || exit

echo "=================================================="
echo "Starting Sentiment Analysis API Server"
echo "=================================================="
echo ""
echo "Model: xlm-roberta-large-xnli (Vietnamese + English)"
echo "Server: http://localhost:5001"
echo ""
echo "First run will download the model (~1.2GB)"
echo "This may take 5-10 minutes on first startup"
echo ""

PYTHON_DIR="humanitarian-logistics/src/main/python"

# Check if requirements are installed
echo "Checking Python dependencies..."
if ! python3 -c "import transformers" 2>/dev/null; then
    echo "Installing requirements..."
    pip install -r "$PYTHON_DIR/requirements.txt" -q
fi

echo ""
echo "=================================================="
echo "Starting API Server (Press Ctrl+C to stop)..."
echo "=================================================="
echo ""

# Run the sentiment API
python3 "$PYTHON_DIR/sentiment_api.py"
