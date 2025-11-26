# Testing Persistence Implementation

## Prerequisites
- Both projects compiled and built
- Java 11+ installed
- Maven available

## Test Case 1: Basic Persistence (humanitarian-logistics)

### Setup
```bash
cd /Users/hieunguyen/OOP_Project/humanitarian-logistics
./run.sh
```

### Steps
1. **Clear any previous data** (first test only):
   ```bash
   rm -rf ~/.humanitarian_logistics/
   ```

2. **Add sample data**:
   - Use "Add Post" feature to add 3-5 sample posts
   - Or use "Import from File" if available
   - Note the number of posts in UI

3. **Add custom disaster**:
   - Go to "Disaster Management" tab
   - Click "➕ Add Disaster Type"
   - Enter: "cyclone"
   - Click OK
   - Verify "cyclone" appears in disaster list

4. **Close application**:
   - Click window close button (NOT kill)
   - Wait for cleanup messages
   - Verify console shows "✓ Data saved successfully"

5. **Verify persistence**:
   ```bash
   ls -lh ~/.humanitarian_logistics/
   # Should see: posts.dat, disasters.dat
   ```

6. **Reopen application**:
   ```bash
   ./run.sh
   ```

7. **Check data**:
   - ✓ All posts from before should appear
   - ✓ Comment count should match
   - ✓ "cyclone" disaster should be in list
   - ✓ Default disasters still present (yagi, matmo, flood, disaster, aid)

### Expected Output
```
Loading posts from persistence...
✓ Loaded X posts from persistence
Loading custom disasters...
✓ Loaded Y custom disaster types
```

---

## Test Case 2: Reset Disasters (humanitarian-logistics)

### Setup
- App running with data from Test Case 1
- At least one custom disaster added

### Steps
1. **Go to Disaster Management tab**

2. **Verify custom disaster exists**:
   - "cyclone" should be visible in the table
   - Comment count should show 0 (no posts tagged with cyclone)

3. **Click "🔄 Reset to Default Disasters" button**

4. **Confirm dialog**:
   - Read warning message
   - Click "Yes" to confirm

5. **Verify results**:
   - ✓ "cyclone" removed from table
   - ✓ 5 default disasters still present
   - ✓ Success dialog shows: "Deleted: 1 custom types"

6. **Close and reopen app**:
   ```bash
   # Close app normally
   # Run: ./run.sh
   ```

7. **Verify reset persisted**:
   - ✓ "cyclone" should NOT reappear
   - ✓ Only default disasters present

### Expected Output
```
Reset complete!
Default disasters restored.
Deleted: 1 custom types.
```

---

## Test Case 3: Reset with Cascade Delete

### Setup
- App running
- Sample posts with disaster type assignments
- Custom disaster created

### Steps
1. **Add posts and assign to custom disaster**:
   - Add sample posts with "cyclone" as disaster type
   - (If using manual UI, note disaster types for posts)

2. **Go to Disaster Management**:
   - Verify "cyclone" shows associated comments in count column

3. **Click Reset**:
   - Confirm the reset action

4. **Verify cascade delete**:
   - ✓ "cyclone" removed
   - ✓ Comments associated with cyclone are deleted
   - ✓ Success message shows comment count

5. **Check posts**:
   - Posts still exist but lose their "cyclone" tag
   - Comments previously tagged with "cyclone" are gone

### Expected Output
```
Reset complete!
Default disasters restored.
Deleted: 1 custom types
Deleted comments: 5
```

---

## Test Case 4: Multiple Custom Disasters

### Setup
- Fresh app (rm -rf ~/.humanitarian_logistics/)

### Steps
1. **Add multiple custom disasters**:
   - "typhoon"
   - "earthquake"
   - "tsunami"

2. **Add sample posts** for each:
   - At least one post per disaster type

3. **Close and reopen**:
   - Verify all 3 custom disasters present
   - Verify sample posts loaded
   - Verify comment counts correct

4. **Reset**:
   - Click "🔄 Reset to Default Disasters"
   - Confirm

5. **Verify all removed**:
   - ✓ All 3 custom disasters gone
   - ✓ Default disasters present
   - ✓ Message shows "Deleted: 3 custom types"

### Expected Output
```
Reset complete!
Default disasters restored.
Deleted: 3 custom types
```

---

## Test Case 5: Independent Data (Both Apps)

### Setup
- Both apps built and ready

### Steps
1. **Start humanitarian-logistics**:
   ```bash
   cd humanitarian-logistics && ./run.sh &
   ```

2. **Add custom disaster**:
   - Go to Disaster Management
   - Add "cyclone"
   - Close app

3. **Start dev-ui**:
   ```bash
   cd dev-ui && ./run.sh &
   ```

4. **Check disaster list**:
   - ✓ "cyclone" should NOT be present
   - ✓ Only default disasters present

5. **Add different custom disaster**:
   - Add "wildfire"
   - Close app

6. **Reopen humanitarian-logistics**:
   ```bash
   cd humanitarian-logistics && ./run.sh
   ```

7. **Verify disaster list**:
   - ✓ "cyclone" present
   - ✓ "wildfire" NOT present
   - ✓ Data independent per app

### Expected Result
Both apps maintain completely separate persistence stores.

---

## Test Case 6: Error Conditions

### Test 6a: Corrupted .dat file

```bash
# Corrupt the posts.dat file
echo "garbage" > ~/.humanitarian_logistics/posts.dat

# Run app
./run.sh
```

**Expected**: App handles error gracefully, shows no posts but doesn't crash

### Test 6b: Missing .dat file

```bash
# Remove only posts.dat
rm ~/.humanitarian_logistics/posts.dat

# Run app
./run.sh
```

**Expected**: App starts with no posts, disasters load normally

### Test 6c: Missing directory

```bash
# Remove entire persistence directory
rm -rf ~/.humanitarian_logistics/

# Run app
./run.sh

# Make changes and close
```

**Expected**: Directory created automatically, data saved on close

---

## Test Case 7: Large Data Set

### Setup
- App running
- Network crawler available

### Steps
1. **Import large dataset**:
   - Use crawler to collect 100+ posts with comments
   - Wait for import to complete

2. **Close app**:
   - Note save time in console

3. **Check file size**:
   ```bash
   ls -lh ~/.humanitarian_logistics/posts.dat
   # Should be MB-sized file
   ```

4. **Reopen app**:
   - Note load time in console
   - Verify all posts appear

5. **Performance check**:
   - ✓ Load time < 5 seconds for 100 posts
   - ✓ Save time < 5 seconds for 100 posts
   - ✓ UI remains responsive

---

## Automated Verification

### Run verification script
```bash
cd /Users/hieunguyen/OOP_Project
chmod +x verify_persistence.sh
./verify_persistence.sh
```

**Expected Output**: All checks pass (17/17)

---

## Manual Compilation Test

```bash
# Test humanitarian-logistics
cd humanitarian-logistics
mvn clean compile
# Should see: BUILD SUCCESS

# Test dev-ui
cd ../dev-ui
mvn clean compile
# Should see: BUILD SUCCESS
```

---

## File Verification

### Check persisted files exist after test
```bash
# After running humanitarian-logistics
ls -la ~/.humanitarian_logistics/
# Expected files:
#   posts.dat (binary file, size varies)
#   disasters.dat (binary file, ~1-2KB)

# After running dev-ui
ls -la ~/.humanitarian_devui/
# Expected files:
#   posts.dat (binary file, size varies)
#   disasters.dat (binary file, ~1-2KB)
```

---

## Cleanup

### After all tests
```bash
# Remove test data
rm -rf ~/.humanitarian_logistics/
rm -rf ~/.humanitarian_devui/

# Remove temp files
rm /tmp/test_persistence.*
```

---

## Troubleshooting Tests

### If posts don't persist
**Checklist**:
1. ✓ Closed app using window close button (not kill)
2. ✓ Console shows "✓ Data saved successfully"
3. ✓ posts.dat file exists: `ls ~/.humanitarian_logistics/posts.dat`
4. ✓ File size > 100 bytes: `ls -lh ~/.humanitarian_logistics/posts.dat`

### If disasters don't persist
**Checklist**:
1. ✓ Added custom disaster before closing
2. ✓ Console shows disaster save message
3. ✓ disasters.dat file exists: `ls ~/.humanitarian_logistics/disasters.dat`
4. ✓ File size > 100 bytes

### If reset doesn't work
**Checklist**:
1. ✓ Button visible in Disaster Management panel
2. ✓ Dialog appears when clicked
3. ✓ Clicked "Yes" in confirmation dialog
4. ✓ Success message appeared

---

## Test Summary Template

```
Test Case: [Name]
Status: [PASS/FAIL]
Date: [YYYY-MM-DD]
Notes: [Any issues or observations]

Persistence Works: [YES/NO]
Reset Works: [YES/NO]
Cascade Delete Works: [YES/NO]
Error Handling: [YES/NO]
```

---

## Success Criteria

### All tests must pass:
- [ ] Test 1: Basic Persistence - PASS
- [ ] Test 2: Reset Disasters - PASS
- [ ] Test 3: Cascade Delete - PASS
- [ ] Test 4: Multiple Custom - PASS
- [ ] Test 5: Independent Data - PASS
- [ ] Test 6: Error Conditions - PASS
- [ ] Test 7: Large Data Set - PASS
- [ ] Verification Script - PASS

### When all tests pass:
✅ Persistence implementation is complete and working correctly
✅ Ready for production deployment
✅ Users can rely on data persistence between sessions
