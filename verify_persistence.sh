#!/bin/bash

# Persistence Implementation Verification Script
# Tests that both applications have persistence integrated correctly

echo "=================================="
echo "Persistence Implementation Check"
echo "=================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASSED=0
FAILED=0

# Function to check file exists
check_file() {
    local file=$1
    local name=$2
    
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $name exists"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗${NC} $name MISSING: $file"
        ((FAILED++))
        return 1
    fi
}

# Function to check method in file
check_method() {
    local file=$1
    local method=$2
    local name=$3
    
    if grep -q "public.*$method" "$file" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} $name: $method() found"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗${NC} $name: $method() NOT found"
        ((FAILED++))
        return 1
    fi
}

echo "1. Checking DataPersistenceManager files..."
check_file "/Users/hieunguyen/OOP_Project/humanitarian-logistics/src/main/java/com/humanitarian/logistics/database/DataPersistenceManager.java" "humanitarian-logistics DataPersistenceManager"
check_file "/Users/hieunguyen/OOP_Project/dev-ui/src/main/java/com/humanitarian/devui/database/DataPersistenceManager.java" "dev-ui DataPersistenceManager"
echo ""

echo "2. Checking Model persistence methods..."
check_method "/Users/hieunguyen/OOP_Project/humanitarian-logistics/src/main/java/com/humanitarian/logistics/ui/Model.java" "loadPersistedData" "humanitarian-logistics Model"
check_method "/Users/hieunguyen/OOP_Project/humanitarian-logistics/src/main/java/com/humanitarian/logistics/ui/Model.java" "savePersistedData" "humanitarian-logistics Model"
check_method "/Users/hieunguyen/OOP_Project/humanitarian-logistics/src/main/java/com/humanitarian/logistics/ui/Model.java" "getPersistenceManager" "humanitarian-logistics Model"
check_method "/Users/hieunguyen/OOP_Project/dev-ui/src/main/java/com/humanitarian/devui/ui/Model.java" "loadPersistedData" "dev-ui Model"
check_method "/Users/hieunguyen/OOP_Project/dev-ui/src/main/java/com/humanitarian/devui/ui/Model.java" "savePersistedData" "dev-ui Model"
check_method "/Users/hieunguyen/OOP_Project/dev-ui/src/main/java/com/humanitarian/devui/ui/Model.java" "getPersistenceManager" "dev-ui Model"
echo ""

echo "3. Checking View save-on-close integration..."
if grep -q "savePersistedData" "/Users/hieunguyen/OOP_Project/humanitarian-logistics/src/main/java/com/humanitarian/logistics/ui/View.java"; then
    echo -e "${GREEN}✓${NC} humanitarian-logistics View: Save on close integrated"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} humanitarian-logistics View: Save on close NOT integrated"
    ((FAILED++))
fi

if grep -q "savePersistedData" "/Users/hieunguyen/OOP_Project/dev-ui/src/main/java/com/humanitarian/devui/ui/View.java"; then
    echo -e "${GREEN}✓${NC} dev-ui View: Save on close integrated"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} dev-ui View: Save on close NOT integrated"
    ((FAILED++))
fi
echo ""

echo "4. Checking DisasterManagementPanel reset functionality..."
if grep -q "resetToDefaultDisasters" "/Users/hieunguyen/OOP_Project/humanitarian-logistics/src/main/java/com/humanitarian/logistics/ui/DisasterManagementPanel.java"; then
    echo -e "${GREEN}✓${NC} humanitarian-logistics DisasterManagementPanel: resetToDefaultDisasters() found"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} humanitarian-logistics DisasterManagementPanel: resetToDefaultDisasters() NOT found"
    ((FAILED++))
fi

if grep -q "resetToDefaultDisasters" "/Users/hieunguyen/OOP_Project/dev-ui/src/main/java/com/humanitarian/devui/ui/DisasterManagementPanel.java"; then
    echo -e "${GREEN}✓${NC} dev-ui DisasterManagementPanel: resetToDefaultDisasters() found"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} dev-ui DisasterManagementPanel: resetToDefaultDisasters() NOT found"
    ((FAILED++))
fi
echo ""

echo "5. Checking reset button UI..."
if grep -q "Reset to Default Disasters" "/Users/hieunguyen/OOP_Project/humanitarian-logistics/src/main/java/com/humanitarian/logistics/ui/DisasterManagementPanel.java"; then
    echo -e "${GREEN}✓${NC} humanitarian-logistics: Reset button UI found"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} humanitarian-logistics: Reset button UI NOT found"
    ((FAILED++))
fi

if grep -q "Reset to Default Disasters" "/Users/hieunguyen/OOP_Project/dev-ui/src/main/java/com/humanitarian/devui/ui/DisasterManagementPanel.java"; then
    echo -e "${GREEN}✓${NC} dev-ui: Reset button UI found"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} dev-ui: Reset button UI NOT found"
    ((FAILED++))
fi
echo ""

echo "6. Checking Application startup integration..."
if grep -q "loadDisasters" "/Users/hieunguyen/OOP_Project/humanitarian-logistics/src/main/java/com/humanitarian/logistics/HumanitarianLogisticsApp.java"; then
    echo -e "${GREEN}✓${NC} humanitarian-logistics App: loadDisasters on startup integrated"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} humanitarian-logistics App: loadDisasters NOT integrated"
    ((FAILED++))
fi

if grep -q "loadDisasters" "/Users/hieunguyen/OOP_Project/dev-ui/src/main/java/com/humanitarian/devui/DevUIApp.java"; then
    echo -e "${GREEN}✓${NC} dev-ui App: loadDisasters on startup integrated"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} dev-ui App: loadDisasters NOT integrated"
    ((FAILED++))
fi
echo ""

echo "7. Checking build status..."
cd /Users/hieunguyen/OOP_Project/humanitarian-logistics
if mvn clean compile -q 2>/dev/null; then
    echo -e "${GREEN}✓${NC} humanitarian-logistics: Builds successfully"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} humanitarian-logistics: Build FAILED"
    ((FAILED++))
fi

cd /Users/hieunguyen/OOP_Project/dev-ui
if mvn clean compile -q 2>/dev/null; then
    echo -e "${GREEN}✓${NC} dev-ui: Builds successfully"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} dev-ui: Build FAILED"
    ((FAILED++))
fi
echo ""

echo "=================================="
echo "Summary"
echo "=================================="
echo -e "Passed: ${GREEN}$PASSED${NC}"
echo -e "Failed: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All persistence checks passed!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Run humanitarian-logistics: cd humanitarian-logistics && ./run.sh"
    echo "2. Run dev-ui: cd dev-ui && ./run.sh"
    echo "3. Test by adding sample data and closing the app"
    echo "4. Reopen the app to verify data persists"
    echo "5. Test reset button in Disaster Management panel"
    exit 0
else
    echo -e "${RED}✗ Some checks failed. Please review the errors above.${NC}"
    exit 1
fi
