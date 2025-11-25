#!/bin/bash

################################################################################
#                    HUMANITARIAN LOGISTICS APP INSTALLER                      #
#                          First-Time Setup Script                             #
#                                                                              #
# This script performs one-time installation and setup on a new machine:       #
# 1. Checks system requirements                                               #
# 2. Installs Java if needed                                                  #
# 3. Installs Python if needed                                                #
# 4. Builds the application                                                   #
# 5. Launches the app                                                         #
################################################################################

set -e  # Exit on error

echo ""
echo "╔════════════════════════════════════════════════════════════════════════╗"
echo "║     HUMANITARIAN LOGISTICS - First Time Installation                    ║"
echo "║                                                                        ║"
echo "║  This is a one-time setup. On subsequent runs, use run.sh directly.   ║"
echo "╚════════════════════════════════════════════════════════════════════════╝"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Detect OS
OS_TYPE=$(uname -s)

print_section() {
    echo ""
    echo -e "${YELLOW}=== $1 ===${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# ============================================================================
# STEP 1: Check and Install Java
# ============================================================================
print_section "Checking Java Installation"

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[^"]*')
    print_success "Java already installed: $JAVA_VERSION"
else
    print_section "Installing Java"
    
    if [ "$OS_TYPE" == "Darwin" ]; then
        # macOS
        if command -v brew &> /dev/null; then
            echo "Installing Java via Homebrew..."
            brew install openjdk@11
            sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
            print_success "Java installed via Homebrew"
        else
            print_error "Homebrew not found. Please install Homebrew first:"
            echo "  /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
            exit 1
        fi
    elif [ "$OS_TYPE" == "Linux" ]; then
        # Linux
        if command -v apt-get &> /dev/null; then
            echo "Installing Java via apt..."
            sudo apt-get update
            sudo apt-get install -y openjdk-11-jdk
            print_success "Java installed via apt"
        elif command -v yum &> /dev/null; then
            echo "Installing Java via yum..."
            sudo yum install -y java-11-openjdk-devel
            print_success "Java installed via yum"
        else
            print_error "Could not install Java. Please install manually."
            exit 1
        fi
    else
        print_error "Unsupported OS: $OS_TYPE"
        exit 1
    fi
fi

# ============================================================================
# STEP 2: Check and Install Maven
# ============================================================================
print_section "Checking Maven Installation"

if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -v 2>&1 | head -1)
    print_success "Maven already installed: $MVN_VERSION"
else
    print_section "Installing Maven"
    
    if [ "$OS_TYPE" == "Darwin" ]; then
        if command -v brew &> /dev/null; then
            echo "Installing Maven via Homebrew..."
            brew install maven
            print_success "Maven installed via Homebrew"
        else
            print_error "Homebrew not found"
            exit 1
        fi
    elif [ "$OS_TYPE" == "Linux" ]; then
        if command -v apt-get &> /dev/null; then
            echo "Installing Maven via apt..."
            sudo apt-get install -y maven
            print_success "Maven installed via apt"
        elif command -v yum &> /dev/null; then
            echo "Installing Maven via yum..."
            sudo yum install -y maven
            print_success "Maven installed via yum"
        else
            print_error "Could not install Maven"
            exit 1
        fi
    fi
fi

# ============================================================================
# STEP 3: Check and Install Python (for sentiment analysis)
# ============================================================================
print_section "Checking Python Installation"

if command -v python3 &> /dev/null; then
    PYTHON_VERSION=$(python3 --version 2>&1)
    print_success "Python already installed: $PYTHON_VERSION"
else
    print_section "Installing Python"
    
    if [ "$OS_TYPE" == "Darwin" ]; then
        if command -v brew &> /dev/null; then
            echo "Installing Python via Homebrew..."
            brew install python@3.9
            print_success "Python installed via Homebrew"
        else
            print_error "Homebrew not found"
            exit 1
        fi
    elif [ "$OS_TYPE" == "Linux" ]; then
        if command -v apt-get &> /dev/null; then
            echo "Installing Python via apt..."
            sudo apt-get install -y python3 python3-pip
            print_success "Python installed via apt"
        elif command -v yum &> /dev/null; then
            echo "Installing Python via yum..."
            sudo yum install -y python3 python3-pip
            print_success "Python installed via yum"
        else
            print_error "Could not install Python"
            exit 1
        fi
    fi
fi

# ============================================================================
# STEP 4: Install Python Dependencies
# ============================================================================
print_section "Installing Python Dependencies"

PYTHON_REQS="src/main/python/requirements.txt"

if [ -f "$PYTHON_REQS" ]; then
    echo "Found Python requirements file..."
    python3 -m pip install --upgrade pip --quiet
    python3 -m pip install -r "$PYTHON_REQS" --quiet
    print_success "Python dependencies installed"
else
    echo "No Python requirements file found (optional)"
fi

# ============================================================================
# STEP 5: Build the Application
# ============================================================================
print_section "Building Application"

echo "Running Maven build (this may take a few minutes)..."
mvn clean compile package -DskipTests -q

if [ $? -eq 0 ]; then
    print_success "Application built successfully"
    JAR_FILE=$(find target -name "*.jar" -type f | head -1)
    if [ -n "$JAR_FILE" ]; then
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        print_success "JAR file created: $JAR_FILE ($JAR_SIZE)"
    fi
else
    print_error "Build failed! Please check Maven installation and try again."
    exit 1
fi

# ============================================================================
# STEP 6: Initialize Databases
# ============================================================================
print_section "Initializing Databases"

# Create empty database files for first-time use
touch humanitarian_logistics_user.db
touch humanitarian_logistics_curated.db

print_success "Database files created"
print_success "  • humanitarian_logistics_user.db (for user data)"
print_success "  • humanitarian_logistics_curated.db (for curated data)"

# ============================================================================
# STEP 7: Installation Complete
# ============================================================================
print_section "Installation Complete!"

echo ""
echo "═══════════════════════════════════════════════════════════════════════════"
echo ""
echo -e "${GREEN}✓ All dependencies installed${NC}"
echo -e "${GREEN}✓ Application built successfully${NC}"
echo -e "${GREEN}✓ Databases initialized${NC}"
echo ""
echo "Next time, you can run the app directly with:"
echo ""
echo "  bash run.sh"
echo ""
echo "═══════════════════════════════════════════════════════════════════════════"
echo ""

# ============================================================================
# STEP 8: Ask to launch app
# ============================================================================
read -p "Would you like to start the application now? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "Starting application..."
    echo ""
    mvn exec:java -Dexec.mainClass="com.humanitarian.logistics.HumanitarianLogisticsApp"
else
    echo ""
    echo "You can start the app later with: bash run.sh"
    echo ""
fi
