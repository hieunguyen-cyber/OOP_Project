# Technology Stack - Humanitarian Logistics UI

## 📋 Danh Sách Công Nghệ Sử Dụng

---

## 1. 🔧 Backend & Core Framework

### Java
- **Version**: Java 11+
- **Purpose**: Main programming language
- **Features Used**:
  - Object-oriented programming (OOP)
  - Design patterns (MVC, Observer, Strategy, etc.)
  - Collections API (List, Map, Set)
  - Stream API
  - Lambda expressions
  - Exception handling
  - Threading & Concurrency
  - Serialization
  - Regular expressions (Regex)

**Why Java?**
- ✅ Strong type safety
- ✅ Object-oriented design
- ✅ Rich ecosystem
- ✅ Platform independent (JVM)
- ✅ Good for desktop applications

---

## 2. 🖥️ GUI Framework

### Java Swing
- **Version**: Built-in with JDK
- **Purpose**: Desktop GUI framework
- **Components Used**:
  
| Component | Purpose |
|-----------|---------|
| `JFrame` | Main application window |
| `JPanel` | Container for layouts |
| `JTabbedPane` | Tabbed interface (Crawl, Data, Comments, Analysis) |
| `JTable` | Data grid (comment display) |
| `JTextArea` | Multi-line text input (keywords, content) |
| `JTextField` | Single-line text input (hashtags, author) |
| `JButton` | Action buttons |
| `JComboBox` | Dropdown selectors (crawler type, analysis module) |
| `JSpinner` | Numeric input (crawl limit) |
| `JLabel` | Static text & status display |
| `JScrollPane` | Scrollable containers |
| `BorderLayout` | Layout manager |
| `GridLayout` | Grid-based layout |
| `FlowLayout` | Flow-based layout |

**Why Swing?**
- ✅ No external dependencies
- ✅ Lightweight & fast
- ✅ Full customization
- ✅ Built-in with JDK
- ✅ Suitable for complex desktop UIs

---

## 3. 📊 Chart & Visualization

### JFreeChart
- **Version**: Latest stable
- **Purpose**: Create and display data visualizations
- **Chart Types Supported**:
  - BarChart (sentiment distribution by category)
  - PieChart (relief item breakdown)
  - LineChart (time series sentiment trends)
  - StackedBarChart (multi-series data)
  - AreaChart (sentiment over time)
  - XYChart (custom x-y plots)

**Key Classes**:
- `JFreeChart` - Main chart object
- `ChartPanel` - Swing component for chart display
- `CategoryDataset` - Data for bar/pie charts
- `XYDataset` - Data for line/scatter charts
- `DefaultPieDataset` - Pie chart data
- `DefaultCategoryDataset` - Category-based data

**Why JFreeChart?**
- ✅ Professional quality charts
- ✅ Multiple chart types
- ✅ Easy data binding
- ✅ Customizable appearance
- ✅ Export capabilities

---

## 4. 🗄️ Database

### SQLite
- **Version**: Latest
- **Purpose**: Local file-based database
- **File Location**: `humanitarian_logistics_user.db`
- **Features Used**:
  - ACID transactions
  - WAL (Write-Ahead Logging)
  - Concurrent access support
  - SQL queries
  - Prepared statements

**Database Schema**:

```sql
-- Posts Table
CREATE TABLE posts (
    post_id TEXT PRIMARY KEY,
    content TEXT NOT NULL,
    author TEXT,
    source TEXT,
    created_at TIMESTAMP,
    sentiment TEXT,
    confidence REAL,
    relief_category TEXT,
    disaster_keyword TEXT
);

-- Comments Table
CREATE TABLE comments (
    comment_id TEXT PRIMARY KEY,
    post_id TEXT NOT NULL,
    content TEXT NOT NULL,
    author TEXT,
    created_at TIMESTAMP,
    sentiment TEXT,
    confidence REAL,
    relief_category TEXT,
    FOREIGN KEY(post_id) REFERENCES posts(post_id)
);
```

**Why SQLite?**
- ✅ No server required
- ✅ File-based (easy backup/transport)
- ✅ Lightweight
- ✅ ACID compliance
- ✅ Good for desktop applications

---

## 5. 🔌 JDBC & Database Connection

### JDBC (Java Database Connectivity)
- **Purpose**: Connect to SQLite database
- **Key Classes**:
  - `java.sql.Connection`
  - `java.sql.Statement`
  - `java.sql.PreparedStatement`
  - `java.sql.ResultSet`
  - `org.sqlite.JDBC` (SQLite driver)

**Connection Pool Management**:
- Singleton pattern for connection management
- Connection reuse
- Proper resource cleanup

---

## 6. 🌐 Web Scraping

### Jsoup
- **Version**: Latest stable
- **Purpose**: HTML parsing and web scraping
- **Features Used**:
  - `Document` - Parse HTML
  - `Element` - Access HTML elements
  - CSS selectors - Find elements
  - Element traversal
  - Text extraction
  - Attribute access

**Usage in YouTube Crawler**:
```java
// Fetch and parse HTML
Document doc = Jsoup.connect(url).get();

// Select elements using CSS selectors
Elements videos = doc.select("a[href*=watch]");
Elements comments = doc.select(".comment-text");

// Extract data
String title = video.text();
String link = video.attr("href");
```

**Why Jsoup?**
- ✅ Simple CSS selector API
- ✅ DOM traversal methods
- ✅ Clean output
- ✅ No JavaScript execution (lightweight)

---

## 7. 🔗 HTTP Client

### OkHttp
- **Version**: Latest stable
- **Purpose**: Make HTTP requests to external services
- **Features Used**:
  - HTTP requests (GET, POST, PUT, DELETE)
  - Request/response handling
  - Connection pooling
  - Retry logic
  - Timeout configuration
  - Header management

**Usage in Sentiment Analysis**:
```java
// Create HTTP client
OkHttpClient client = new OkHttpClient();

// Build request
Request request = new Request.Builder()
    .url("http://localhost:5001/sentiment")
    .post(body)
    .build();

// Execute request
Response response = client.newCall(request).execute();
```

**Why OkHttp?**
- ✅ Modern HTTP client
- ✅ Connection pooling
- ✅ Interceptor support
- ✅ Automatic retries
- ✅ Built-in compression

---

## 8. 📝 JSON Processing

### GSON (Google Gson)
- **Version**: Latest stable
- **Purpose**: JSON serialization and deserialization
- **Features Used**:
  - Object to JSON conversion
  - JSON to Object conversion
  - Custom serializers
  - Custom deserializers
  - Nested object handling

**Usage Examples**:
```java
// Java object to JSON
Gson gson = new Gson();
String json = gson.toJson(post);

// JSON to Java object
Post post = gson.fromJson(jsonString, Post.class);

// Collection handling
List<Post> posts = gson.fromJson(
    jsonArray, 
    new TypeToken<List<Post>>(){}.getType()
);
```

**Why GSON?**
- ✅ Easy to use
- ✅ No configuration needed
- ✅ Supports generics
- ✅ Good error messages
- ✅ Lightweight

---

## 9. 🐍 Python Backend Service

### Python 3.8+
- **Version**: 3.8 or higher
- **Purpose**: NLP and ML model inference
- **Location**: `src/main/python/sentiment_api.py`
- **Port**: `localhost:5001`

### Flask
- **Purpose**: REST API framework for Python service
- **Features**:
  - HTTP endpoints for sentiment analysis
  - Category classification
  - Request/response handling
  - CORS support

### Transformers (Hugging Face)
- **Model**: `xlm-roberta-large-xnli`
- **Purpose**: Multilingual zero-shot classification
- **Features**:
  - 100+ language support
  - Zero-shot text classification
  - Sentiment analysis
  - Category classification

**Key Endpoints**:
- `/sentiment` - Analyze text sentiment
- `/sentiment_batch` - Analyze multiple texts
- `/categorize` - Classify relief category
- `/health` - Check service status

**Why Python + Transformers?**
- ✅ Pre-trained models available
- ✅ Multilingual support (Vietnamese & English)
- ✅ High accuracy
- ✅ Easy model loading
- ✅ Good ecosystem for NLP

---

## 10. 🏗️ Build & Project Management

### Maven
- **Version**: 3.6+
- **Purpose**: Build automation and dependency management
- **Configuration File**: `pom.xml`

**Key Features**:
- Dependency management
- Build lifecycle (compile, test, package)
- Plugin execution
- Profile management
- Repository management

**Standard Build Commands**:
```bash
mvn clean compile      # Compile source code
mvn clean test        # Run unit tests
mvn clean package     # Create JAR file
mvn clean install     # Install to local repository
```

**Why Maven?**
- ✅ Standard Java build tool
- ✅ Dependency management
- ✅ Plugin ecosystem
- ✅ Convention over configuration

---

## 11. 🧪 Testing Framework

### JUnit 4 / JUnit 5
- **Purpose**: Unit testing framework
- **Features**:
  - Test annotations (`@Test`, `@Before`, `@After`)
  - Assertions
  - Test suites
  - Parameterized tests
  - Fixtures

**Test Categories**:
- Model tests (Post, Comment, Sentiment)
- Crawler tests (YouTubeCrawler, MockCrawler)
- Sentiment analyzer tests
- Database tests
- UI component tests

---

## 12. 📚 Logging

### java.util.logging (JUL)
- **Purpose**: Application logging
- **Log Levels**:
  - `SEVERE` - Critical errors
  - `WARNING` - Warnings
  - `INFO` - Informational messages
  - `FINE` - Detailed information
  - `FINER` - Very detailed
  - `FINEST` - Most detailed

**Usage**:
```java
private static final Logger logger = 
    Logger.getLogger(ClassName.class.getName());

logger.info("User started crawling");
logger.warning("API returned error: " + error);
logger.severe("Database connection failed");
```

**Why JUL?**
- ✅ Built-in with JDK
- ✅ No external dependencies
- ✅ Configurable via properties file
- ✅ Multiple handlers (file, console, etc.)

---

## 13. 🔐 Data Serialization

### Java Serialization
- **Purpose**: Save/load objects from storage
- **Implements**:
  - `Serializable` interface
  - `ObjectOutputStream`
  - `ObjectInputStream`

**Used For**:
- Cache posts/comments
- Session persistence
- Data export

---

## 14. 🛠️ IDE & Development Tools

### IntelliJ IDEA / Eclipse
- **Purpose**: Java IDE
- **Features**:
  - Code completion
  - Refactoring tools
  - Debugging
  - Built-in terminal
  - Git integration

### Git
- **Purpose**: Version control
- **Features**:
  - Commit history
  - Branch management
  - Remote repositories (GitHub)
  - Merge & conflict resolution

---

## 15. 📦 Key Dependencies

### pom.xml Dependencies

```xml
<dependencies>
    <!-- Database -->
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
        <version>3.39.0</version>
    </dependency>

    <!-- Web Scraping -->
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>1.15.3</version>
    </dependency>

    <!-- HTTP Client -->
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>4.10.0</version>
    </dependency>

    <!-- JSON Processing -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>

    <!-- Charts -->
    <dependency>
        <groupId>org.jfree</groupId>
        <artifactId>jfreechart</artifactId>
        <version>1.5.3</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 16. 🐳 Containerization (Optional)

### Docker
- **Purpose**: Container packaging
- **Use Case**: Deploy application in containers
- **Components**:
  - Java runtime
  - Application JAR
  - Python sentiment service
  - Database volume

---

## 17. 📊 System Architecture Technology Stack

```
┌────────────────────────────────────────────────┐
│         PRESENTATION LAYER                    │
│  Java Swing + JFreeChart for Visualization   │
└────────────────────────────────────────────────┘
                      │
                      ├─ HTTP Requests (OkHttp)
                      │
┌────────────────────────────────────────────────┐
│       BUSINESS LOGIC LAYER                    │
│  Java OOP + Design Patterns                   │
│  - MVC Pattern                                │
│  - Observer Pattern                           │
│  - Strategy Pattern                           │
│  - Registry Pattern                           │
│  - Factory Pattern                            │
│  - Singleton Pattern                          │
└────────────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
    ┌─────────┐  ┌────────┐  ┌──────────┐
    │ Jsoup   │  │ GSON   │  │Database  │
    │Crawling │  │JSON    │  │Manager   │
    └─────────┘  └────────┘  └──────────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
┌────────────────────────────────────────────────┐
│          DATA LAYER                           │
│  SQLite Database + JDBC Connection            │
│  - Posts Table                                │
│  - Comments Table                             │
│  - Data Persistence                           │
└────────────────────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        │                           │
        ▼                           ▼
  ┌──────────────┐       ┌──────────────────┐
  │ Python Flask │       │ File System      │
  │NLP Service   │       │SQLite DB Files   │
  │xlm-roberta   │       │Application Logs  │
  │Model         │       │Cache Files       │
  └──────────────┘       └──────────────────┘
```

---

## 18. 📋 Technology Comparison Matrix

| Aspect | Technology | Reason |
|--------|-----------|--------|
| **Language** | Java 11+ | Strong OOP, Type-safe, Cross-platform |
| **GUI** | Java Swing | No external GUI library needed, Lightweight |
| **Charts** | JFreeChart | Professional visualizations, Multiple types |
| **Database** | SQLite | File-based, No server, Lightweight |
| **Scraping** | Jsoup | Simple API, HTML parsing |
| **HTTP** | OkHttp | Modern, Connection pooling |
| **JSON** | GSON | Simple, No config, Lightweight |
| **Build** | Maven | Standard, Dependency management |
| **NLP** | Python + Transformers | Pre-trained models, Multilingual |
| **Web Framework (Python)** | Flask | Lightweight, Minimal dependencies |

---

## 19. 🔄 Integration Points

### Java ↔ Python Communication
```
Java Application
    │
    ├─ OkHttp Client
    │
    ├─ HTTP POST request
    │  └─ Text to analyze
    │
    ▼
Python Flask Service (localhost:5001)
    │
    ├─ Request handler
    │
    ├─ Load xlm-roberta model
    │
    ├─ Process text
    │
    └─ Return JSON response
         └─ Sentiment score + confidence
         └─ Category classification

Java Application
    │
    ├─ Parse JSON (GSON)
    │
    ├─ Create Sentiment/ReliefItem objects
    │
    └─ Update Model & UI
```

### Database Operations
```
Java Application
    │
    ├─ Create DatabaseManager (Singleton)
    │
    ├─ JDBC Connection to SQLite
    │
    ├─ Execute SQL queries
    │  ├─ INSERT posts/comments
    │  ├─ SELECT for retrieval
    │  ├─ UPDATE sentiment
    │  └─ DELETE operations
    │
    └─ ResultSet to Object mapping
         └─ Post, Comment objects
```

---

## 20. 🚀 Performance Optimizations

### Technology-specific Optimizations

| Technology | Optimization |
|-----------|---------------|
| **Swing** | Use SwingWorker for long operations |
| **JDBC** | Use PreparedStatement to prevent SQL injection |
| **OkHttp** | Connection pooling, HTTP caching |
| **GSON** | Streaming large JSON files |
| **SQLite** | Index frequently queried columns |
| **JFreeChart** | Lazy chart rendering, cache chart images |
| **Python** | Model caching, Batch inference |

---

## 21. 📦 Dependency Tree Summary

```
Humanitarian-Logistics-App
│
├── Java Core Libraries
│   ├── java.util (Collections, Logging)
│   ├── java.sql (Database)
│   ├── java.io (Serialization)
│   ├── java.time (DateTime)
│   ├── java.nio (File operations)
│   └── java.util.regex (Pattern matching)
│
├── GUI Framework
│   └── javax.swing (All components)
│
├── External Libraries
│   ├── Jsoup 1.15.3 (HTML parsing)
│   ├── OkHttp 4.10.0 (HTTP client)
│   ├── GSON 2.10.1 (JSON)
│   ├── JFreeChart 1.5.3 (Charts)
│   ├── SQLite JDBC 3.39.0 (Database)
│   └── JUnit 4.13.2 (Testing)
│
├── Build Tool
│   └── Maven 3.6+ (POM management)
│
└── External Services
    └── Python Flask Service
        ├── Python 3.8+
        ├── Flask (Web framework)
        ├── Transformers (HuggingFace)
        └── xlm-roberta-large-xnli (Model)
```

---

## 22. 🔗 External APIs & Services

| Service | Endpoint | Protocol | Purpose |
|---------|----------|----------|---------|
| YouTube | youtube.com | HTTPS + Jsoup | Web scraping videos & comments |
| Sentiment API | localhost:5001/sentiment | HTTP/JSON | Sentiment analysis |
| Category API | localhost:5001/categorize | HTTP/JSON | Relief category classification |

---

## Summary

### Frontend
- ✅ **Java Swing** - Desktop UI framework
- ✅ **JFreeChart** - Data visualization

### Backend
- ✅ **Java 11+** - Core application logic
- ✅ **Design Patterns** - MVC, Observer, Strategy, etc.

### Data Management
- ✅ **SQLite** - Local database
- ✅ **JDBC** - Database connectivity

### External Integration
- ✅ **Jsoup** - Web scraping
- ✅ **OkHttp** - HTTP communication
- ✅ **GSON** - JSON serialization

### Advanced Features
- ✅ **Python + Transformers** - NLP/ML inference
- ✅ **JFreeChart** - Professional charts

### Build & Testing
- ✅ **Maven** - Build automation
- ✅ **JUnit** - Unit testing

