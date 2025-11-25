# Humanitarian Logistics Application

A Java-based humanitarian disaster response analysis system with sentiment analysis and data visualization.

## 🚀 Quick Start

### First-Time Installation (New Machine)

Simply run:
```bash
bash install.sh
```

This will automatically:
- Check and install required dependencies (Java, Maven, Python)
- Build the application
- Create necessary data files
- Launch the application

### Subsequent Runs

Run the application anytime with:
```bash
cd humanitarian-logistics
bash run.sh
```

## 📋 Features

- **Data Collection Panel**: Manually add posts and comments
- **Use Our Database**: Load 31 curated posts from different disasters
- **Problem 1 Analysis**: Public satisfaction metrics per relief category
- **Problem 2 Analysis**: Temporal sentiment tracking over time
- **Visualization**: Multiple charts and statistics

## 🗂️ Project Structure

```
humanitarian-logistics/
├── src/
│   ├── main/
│   │   ├── java/          # Java source code
│   │   ├── python/        # Python sentiment analysis
│   │   └── resources/
│   └── test/
├── pom.xml                 # Maven configuration
├── run.sh                  # Application launcher
└── target/                 # Build artifacts (generated)
```

## 💾 Database Files

The application uses two separate database files:

- **humanitarian_logistics_user.db** - Stores user-entered data
- **humanitarian_logistics_curated.db** - Reserved for curated datasets

Both are created automatically on first run.

## 🔧 Requirements

- **Java**: 11 or higher
- **Maven**: 3.6 or higher
- **Python**: 3.9 or higher (for sentiment analysis)

## 📝 Usage

1. Launch the application
2. Navigate to the "Data Collection" tab
3. Choose one of:
   - **Add Post/Comment**: Manually enter new data
   - **Use Our Database**: Load 31 pre-curated disaster posts
4. Go to Analysis tabs to view statistics and visualizations

## 🐛 Troubleshooting

If the installer fails:

1. Ensure you have internet connection (for dependency downloads)
2. Check Java/Maven/Python installation: `java -version`, `mvn -version`, `python3 --version`
3. Make sure you have write permissions in the project directory
4. Delete `.installed` marker file and try again if scripts are cached

## 📦 Building from Source

To manually build the project:

```bash
cd humanitarian-logistics
mvn clean package -DskipTests
```

This creates a JAR file in the `target/` directory.

## 🎯 Development

The application demonstrates:
- **Object-Oriented Design**: Proper use of classes, inheritance, and polymorphism
- **MVC Pattern**: Separation of concerns with Model-View-Controller
- **Sentiment Analysis**: Integration with Python for NLP
- **Data Visualization**: Swing-based charts and statistics
- **Database Integration**: SQLite for data persistence

---

For more information, see the humanitarian-logistics README.
