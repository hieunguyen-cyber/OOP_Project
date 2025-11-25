# Installation & Setup Guide

## 📦 Distribution Package

The application is packaged as `humanitarian-logistics-app.tar.gz` (79 KB) which contains everything needed to run on a new machine.

## 🚀 First-Time Setup on New Machine

### Step 1: Extract Package

```bash
tar -xzf humanitarian-logistics-app.tar.gz
cd humanitarian-logistics-app  # or whatever directory it extracts to
```

### Step 2: Run Installer (One-Time)

```bash
bash install.sh
```

The installer will:

1. **Check Dependencies**
   - Java (OpenJDK 11) - required
   - Maven (3.6+) - required
   - Python (3.9+) - required for sentiment analysis

2. **Install Missing Dependencies**
   - On macOS: Uses Homebrew
   - On Linux: Uses apt-get or yum
   - Prompts for sudo password if needed

3. **Build Application**
   - Downloads Maven dependencies
   - Compiles Java source code
   - Creates JAR files
   - Sets up database files

4. **Create Marker File**
   - Creates `.installed` file to mark setup as complete
   - This prevents re-running installer unnecessarily

5. **Launch Application**
   - Automatically starts the GUI application
   - Shows Swing window for data entry and analysis

### Step 3: Using the Application

Once the GUI appears:

1. **Data Collection Tab**
   - Click "Add Post/Comment" to manually enter data
   - Or click "Use Our Database" to load 31 curated posts

2. **Analysis Tabs**
   - View satisfaction analysis per relief category
   - See temporal sentiment evolution
   - Check statistics and visualizations

3. **Data Persistence**
   - User-entered data saved to `humanitarian_logistics_user.db`
   - Curated data loads to memory (no persistence)

---

## 🔄 Running Application Again

After first-time setup, simply run:

```bash
cd humanitarian-logistics
bash run.sh
```

The script checks for `.installed` marker and:
- If found: Skips installer, goes directly to app build and launch
- If missing: Runs installer again (shouldn't happen)

---

## ⚙️ Requirements

### Minimum
- **Memory**: 512 MB
- **Disk Space**: 500 MB (for dependencies and build)
- **Network**: Required for first-time setup (Maven downloads)

### Operating Systems
- ✅ macOS (10.12+)
- ✅ Linux (Ubuntu 18.04+, CentOS 7+, etc.)
- ✅ Windows (with WSL or Git Bash)

### Software
- **Java**: 11 or higher
- **Maven**: 3.6 or higher  
- **Python**: 3.9 or higher
- **Bash**: 4.0 or higher

---

## 🐛 Troubleshooting

### Installer Hangs or Freezes

**Solution**: Ctrl+C to cancel, then:
```bash
rm .installed
bash install.sh
```

### "Java not found" after installation

**macOS with Homebrew:**
```bash
brew install openjdk@11
```

**Linux (Ubuntu):**
```bash
sudo apt-get install openjdk-11-jdk
```

### "Maven command not found"

**macOS:**
```bash
brew install maven
```

**Linux (Ubuntu):**
```bash
sudo apt-get install maven
```

### Python dependency issues

If sentiment analysis fails, install Python packages:
```bash
cd humanitarian-logistics/src/main/python
pip install -r requirements.txt
```

### Permission denied on install.sh

```bash
chmod +x install.sh
bash install.sh
```

### Database file errors

The `.db` files are created automatically on first run. If issues:
```bash
cd humanitarian-logistics
rm -f *.db
bash run.sh
```

---

## 📊 Project Structure After Setup

```
humanitarian-logistics/
├── src/               # Source code (Java, Python)
├── target/            # Build artifacts (generated)
├── pom.xml            # Maven config
├── run.sh             # Launch script
├── install.sh         # Setup script
├── humanitarian_logistics_user.db      # User data
└── humanitarian_logistics_curated.db   # Curated data
```

---

## 🔐 Important Notes

1. **Network Required**: First install needs internet for Maven dependencies
2. **Admin Might Be Needed**: Installing system packages may require sudo
3. **One-Time Setup**: `.installed` marker prevents re-running installer
4. **Clean Build**: Each `run.sh` rebuilds with `mvn clean package`
5. **Data Isolated**: Two separate DB files prevent user/curated data mixing

---

## 🎯 Verification Checklist

After installation:

- [ ] Application window appears
- [ ] "Data Collection" tab is visible
- [ ] "Use Our Database" button is green
- [ ] Can click button and see confirmation dialog
- [ ] 31 posts load successfully
- [ ] Analysis tabs show statistics
- [ ] Two database files exist: `humanitarian_logistics_user.db` and `humanitarian_logistics_curated.db`

---

## 📞 Support

If issues persist:

1. Check that all requirements are installed:
   ```bash
   java -version
   mvn -version
   python3 --version
   ```

2. Check for error messages in console

3. Ensure project directory has write permissions

4. Try clean rebuild:
   ```bash
   rm -rf humanitarian-logistics/target
   rm .installed
   bash install.sh
   ```

---

For detailed project information, see `PROJECT_STRUCTURE.txt` and `README.md`.
