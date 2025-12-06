# Giải Thích Thiết Kế Hệ Thống - Humanitarian Logistics UI

## 📑 Mục Lục
1. [Tổng Quan Kiến Trúc](#tổng-quan-kiến-trúc)
2. [Packages & Responsibilities](#packages--responsibilities)
3. [Chi Tiết Các Packages](#chi-tiết-các-packages)
4. [Design Patterns Sử Dụng](#design-patterns-sử-dụng)
5. [Flow & Interactions](#flow--interactions)
6. [Technology Stack](#technology-stack)

---

## 🏗️ Tổng Quan Kiến Trúc

### Kiến Trúc Tổng Thể

```
┌─────────────────────────────────────────────────────────────────┐
│                    Humanitarian Logistics UI                    │
│                         (Java Swing)                            │
└─────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                            │
│                                                                       │
│  ┌─────────┐  ┌──────────────┐  ┌─────────────────┐  ┌────────────┐   │
│  │  View   │  │CrawlControl  │  │DataCollection   │  │ Advanced   │   │
│  │(JFrame) │  │Panel         │  │Panel            │  │ Analysis   │   │
│  └─────────┘  └──────────────┘  └─────────────────┘  └────────────┘   │
│                                                                       │
│  ┌───────────────────────────────────────────────────────────────┐    │
│  │         Comment Management Panel + Utility Classes            │    │
│  └───────────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────────────┘
                                   │
                                   │ uses
                                   ▼
┌───────────────────────────────────────────────────────────────────────┐
│                      BUSINESS LOGIC LAYER                             │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                    Model (MVC Core)                             │  │
│  │  - Manages Posts & Comments                                     |  │
│  │  - Coordinates sentiment analysis                               |  │
│  │  - Coordinates category classification                          |  │
│  │  - Manages analysis modules                                     |  │
│  │  - Notifies UI listeners on changes                             │  | 
│  └─────────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌────────────┬──────────────┬────────────┬──────────────────┐        │
│  │  Crawler   │  Sentiment   │  Category  │    Analysis      │        │
│  │  System    │  Analyzer    │Classifier  │    Modules       │        │
│  └────────────┴──────────────┴────────────┴──────────────────┘        │
└───────────────────────────────────────────────────────────────────────┘
                                   │
                                   │ uses
                                   ▼
┌───────────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                                     │
│                                                                       │
│  ┌──────────────┐  ┌─────────────────┐  ┌─────────────────────┐       │
│  │  Database    │  │  Persistence    │  │  Model Objects      │       │
│  │  Manager     │  │  Manager        │  │  (Post, Comment,    │       │
│  │(SQLite)      │  │                 │  │   Sentiment, etc)   │       │
│  └──────────────┘  └─────────────────┘  └─────────────────────┘       │
└───────────────────────────────────────────────────────────────────────┘
```

### Mô Hình MVC

```
┌──────────────────────────────────────────────────────────────┐
│                     Humanitarian Logistics UI                │
│                                                              │
│  M (Model)              V (View)       C (Controller)        │
│  ──────────             ────────       ──────────────        │
│  • Model.java           • View.java    • CrawlControlPanel   │
│  • Business logic       • JFrame       • DataCollectionP.    │
│  • Data management      • UI layout    • CommentMgmtPanel    │
│  • Notifications        • Listeners    • Analysis Panel      │
│                                                              │
│  ┌─────────────────────────────────────────────────────────┐ |
│  │ Data Flow:                                              │ |
│  │                                                         │ |
│  │ User Input                                              │ |
│  │    ↓                                                    │ |
│  │ Controller (Panel) ← handles user action                │ |
│  │    ↓                                                    │ |
│  │ Model ← processes business logic                        │ |
│  │    ↓                                                    │ |
│  │ View ← gets updated via ModelListener                   │ |
│  │    ↓                                                    │ |
│  │ User sees changes                                       │ |
│  └─────────────────────────────────────────────────────────┘ |
└──────────────────────────────────────────────────────────────┘
```

---

## 📦 Packages & Responsibilities

### 1. **UI Package** (`com.humanitarian.logistics.ui`)
**Responsibility**: Presentation layer & user interaction

**Key Classes**:
- `View` (JFrame) - Main application window
- `Model` - MVC business logic core
- `ModelListener` - Observer pattern interface
- `CrawlControlPanel` - Web crawling interface
- `DataCollectionPanel` - Manual data entry
- `CommentManagementPanel` - Comment management
- `AdvancedAnalysisPanel` - Analysis visualization
- `CrawlingUtility` - Helper methods
- `ChartsUtility` - Chart creation utilities
- `InteractiveChartUtility` - Interactive chart support

**Interactions**:
```
View (JFrame)
    ├── CrawlControlPanel (uses Model, Crawler)
    ├── DataCollectionPanel (uses Model)
    ├── CommentManagementPanel (uses Model)
    └── AdvancedAnalysisPanel (uses Model, Analysis modules)

Model
    ├── notifies → ModelListener
    ├── uses → SentimentAnalyzer
    ├── uses → PythonCategoryClassifier
    ├── uses → DatabaseManager
    └── uses → AnalysisModule
```

---

### 2. **Model Package** (`com.humanitarian.logistics.model`)
**Responsibility**: Core data structures & domain models

**Key Classes**:

#### `Post` (Abstract Base Class)
```
Purpose: Base class for all social media posts
Attributes:
  - postId: String (final, immutable)
  - content: String (final, immutable)
  - createdAt: LocalDateTime (final, immutable)
  - author: String (final, immutable)
  - source: String (final, immutable)
  - sentiment: Sentiment (mutable)
  - reliefItem: ReliefItem (mutable)
  - disasterKeyword: String (mutable)
  - comments: List<Comment> (mutable list)

Why Abstract:
  ✓ Cannot instantiate Post directly (specific platform posts only)
  ✓ All posts share common structure
  ✓ Subclasses add platform-specific attributes

Key Methods:
  - getComments(): returns unmodifiable list (encapsulation)
  - addComment(Comment): controlled modification
  - setSentiment(), setReliefItem(): validation & notification
```

#### `YouTubePost` (Concrete Subclass)
```
Purpose: YouTube-specific post implementation
Additional Attributes:
  - channelId: String (YouTube channel)
  - likes: int (YouTube metric)
  - views: int (YouTube metric)
  - disasterType: DisasterType (linked disaster)

Why Subclass:
  ✓ Reuses Post's common behavior
  ✓ Adds YouTube-specific metadata
  ✓ Polymorphism - Post[] can contain YouTubePosts
```

#### `Comment`
```
Purpose: Comment on posts
Attributes:
  - commentId: String (final, unique)
  - postId: String (final, parent reference)
  - content: String (final, immutable)
  - author: String (final, immutable)
  - createdAt: LocalDateTime (final, immutable)
  - sentiment: Sentiment (mutable, analyzed)
  - reliefItem: ReliefItem (mutable, classified)

Why Separate Class:
  ✓ Comments have different lifecycle than posts
  ✓ Can be created, edited, deleted independently
  ✓ Need separate table in database
```

#### `Sentiment`
```
Purpose: Sentiment analysis result
Attributes:
  - type: SentimentType (POSITIVE, NEGATIVE, NEUTRAL)
  - confidence: double (0.0 to 1.0, higher = more confident)
  - text: String (analyzed text)

Why Separate Class:
  ✓ Encapsulates sentiment analysis result
  ✓ Reusable for Post and Comment
  ✓ Supports multiple sentiment analyzers
```

#### `ReliefItem`
```
Purpose: Relief/aid category classification
Attributes:
  - category: Category (ENUM: CASH, MEDICAL, SHELTER, FOOD, TRANSPORTATION)
  - description: String (relief item description)
  - priority: int (1-5 priority level)

Why ENUM Category:
  ✓ Limited to 5 predefined categories (domain constraint)
  ✓ Type-safe (no invalid categories)
  ✓ Easy validation
```

#### `DisasterType`
```
Purpose: Disaster type management
Attributes:
  - name: String (disaster name)
  - aliases: List<String> (alternative names, hashtags)

Purpose:
  ✓ Represents disaster events (Yagi, Matmo, etc.)
  ✓ Matches posts to disasters using keyword/alias matching
  ✓ Supports dynamic disaster creation
```

**Relationships**:
```
Post
  ├─ HAS-A Sentiment (optional)
  ├─ HAS-A ReliefItem (optional)
  ├─ HAS-A List<Comment>
  └─ HAS-A DisasterType (optional)

Comment
  ├─ HAS-A Sentiment (optional)
  ├─ HAS-A ReliefItem (optional)
  └─ References Post (via postId)
```

---

### 3. **Crawler Package** (`com.humanitarian.logistics.crawler`)
**Responsibility**: Data collection from multiple sources

**Architecture**:
```
DataCrawler (Interface)
    │
    ├─ YouTubeCrawler (Implementation)
    │   └─ Web scraping using Jsoup
    │   └─ Extracts: title, comments, likes, views
    │
    ├─ MockDataCrawler (Implementation)
    │   └─ Generates test data
    │   └─ For development & testing
    │
    └─ CrawlerRegistry (Factory + Registry)
        └─ Manages crawler instances
        └─ Dynamic registration
```

**Key Classes**:

#### `DataCrawler` (Interface)
```
Purpose: Contract for all crawlers
Methods:
  - crawlPosts(keywords, hashtags, limit): List<Post>
    └─ Returns list of posts matching search
  - getCrawlerName(): String
    └─ Identifies crawler type
  - isInitialized(): boolean
    └─ Check if crawler is ready
  - shutdown(): void
    └─ Cleanup resources

Why Interface:
  ✓ Different sources (YouTube, Facebook, Twitter)
  ✓ Multiple implementations
  ✓ Easy to test with mock
  ✓ Loose coupling to UI
```

#### `YouTubeCrawler` (Implementation)
```
Purpose: Scrape data from YouTube
Data Extraction:
  - Video title, description
  - Video statistics (views, likes)
  - Comments on videos
  - Comment author, timestamp, sentiment indicators

Technology:
  - Jsoup (HTML parsing)
  - Regex (pattern extraction)
  - OkHttp (HTTP requests)

Why Separate from Other Crawlers:
  ✓ YouTube-specific HTML structure
  ✓ YouTube-specific data format
  ✓ Can be swapped for API-based crawler
```

#### `MockDataCrawler` (Implementation)
```
Purpose: Generate test data for development
Features:
  - Creates random posts with realistic content
  - Generates comments for testing
  - Useful when YouTube API unavailable
  - Implements same interface as YouTubeCrawler

Use Case:
  ✓ Development without internet connection
  ✓ Testing analysis modules
  ✓ Demonstration & training
```

#### `CrawlerRegistry` (Singleton + Factory)
```
Purpose: Central crawler management
Pattern: Registry Pattern + Factory Pattern
Features:
  - Single instance (Singleton)
  - Maps crawler names to implementations
  - Dynamic crawler registration
  - Creates crawler instances on demand

Why This Pattern:
  ✓ UI doesn't know about specific crawlers
  ✓ Easy to add new crawlers
  ✓ Crawlers auto-discovered by UI
  ✓ Configuration-driven
```

**Usage Example**:
```java
// Initialize crawlers once
CrawlerManager.initializeCrawlers();

// Get registry
CrawlerRegistry registry = CrawlerRegistry.getInstance();

// Dynamic crawler selection
String crawlerName = "YOUTUBE";  // from UI combo box
DataCrawler crawler = registry.createCrawler(crawlerName);
List<Post> posts = crawler.crawlPosts(keywords, hashtags, limit);

// Fallback mechanism
try {
    posts = crawler.crawlPosts(...);
} catch (Exception e) {
    // Fallback to mock crawler
    DataCrawler mockCrawler = registry.createCrawler("MOCK");
    posts = mockCrawler.crawlPosts(...);
}
```

---

### 4. **Sentiment Package** (`com.humanitarian.logistics.sentiment`)
**Responsibility**: Text sentiment analysis

**Architecture**:
```
SentimentAnalyzer (Interface)
    │
    ├─ EnhancedSentimentAnalyzer (Keyword-based)
    │   └─ English & Vietnamese keywords
    │   └─ Fast, no dependencies
    │
    ├─ PythonSentimentAnalyzer (ML-based)
    │   └─ xlm-roberta model
    │   └─ Calls Python backend
    │   └─ Better accuracy
    │
    └─ SimpleSentimentAnalyzer (Emoji-based)
        └─ Emoji sentiment detection
        └─ Lightweight
```

**Key Classes**:

#### `SentimentAnalyzer` (Interface)
```
Purpose: Contract for sentiment analysis
Methods:
  - analyzeSentiment(text): Sentiment
    └─ Single text analysis
  - analyzeSentimentBatch(texts[]): Sentiment[]
    └─ Batch analysis (performance)
  - getModelName(): String
    └─ Model identifier
  - initialize(): void
    └─ Setup (load models, connect to services)
  - shutdown(): void
    └─ Cleanup (release connections)

Why Interface:
  ✓ Multiple analysis methods available
  ✓ Can switch at runtime
  ✓ Easy to compare accuracy
  ✓ Plug-in architecture
```

#### `EnhancedSentimentAnalyzer` (Keyword-Based)
```
Method: Keyword/dictionary-based
Algorithm:
  1. Convert text to lowercase
  2. Count positive keywords (good, great, help, relief, etc.)
  3. Count negative keywords (bad, poor, fail, disaster, etc.)
  4. Determine type based on counts
  5. Calculate confidence based on keyword frequency

Pros:
  ✓ Fast (no ML overhead)
  ✓ No dependencies
  ✓ Interpretable (can see why)
  ✓ Works offline

Cons:
  ✗ Lower accuracy
  ✗ Limited to known words
  ✗ Doesn't understand context
```

#### `PythonSentimentAnalyzer` (ML-Based)
```
Method: Machine Learning via Python backend
Model: xlm-roberta-large-xnli
  - Multilingual (Vietnamese, English, 100+ languages)
  - Zero-shot classification
  - Pre-trained transformer model

Process:
  1. Send text to Python API (http://localhost:5001)
  2. Python service loads xlm-roberta model
  3. Model performs inference
  4. Return sentiment probability

Pros:
  ✓ High accuracy
  ✓ Understands context
  ✓ Multilingual support
  ✓ Zero-shot (works for any domain)

Cons:
  ✗ Slower (ML inference)
  ✗ Requires Python service running
  ✗ Higher resource usage
```

#### `PythonCategoryClassifier` (Relief Category)
```
Purpose: Classify post/comment into relief categories
Categories (5 types):
  - CASH: Money assistance
  - MEDICAL: Medical/health support
  - SHELTER: Housing/accommodation
  - FOOD: Food assistance
  - TRANSPORTATION: Transport/logistics

Method:
  1. Call Python API with text
  2. API returns category probabilities
  3. Validation: only accept 5 predefined categories
  4. Fallback: default to FOOD if invalid
  5. Return ReliefItem with category

Validation:
  try {
      ReliefItem.Category category = ReliefItem.Category.valueOf(result);
      return new ReliefItem(category, description, priority);
  } catch (IllegalArgumentException e) {
      // Invalid category from API
      return new ReliefItem(ReliefItem.Category.FOOD, description, priority);
  }

Why Separate from Sentiment:
  ✓ Different purpose (sentiment vs classification)
  ✓ Different model (sentiment model vs category model)
  ✓ Can be disabled independently
```

---

### 5. **Analysis Package** (`com.humanitarian.logistics.analysis`)
**Responsibility**: Data analysis & insights generation

**Architecture**:
```
AnalysisModule (Interface)
    │
    ├─ SatisfactionAnalysisModule (Problem 1 Solution)
    │   └─ Relief category sentiment analysis
    │   └─ Satisfaction scoring per category
    │   └─ Effectiveness assessment
    │
    └─ TimeSeriesSentimentModule (Problem 2 Solution)
        └─ Temporal sentiment tracking
        └─ Trend analysis
        └─ Timeline visualization
```

**Key Classes**:

#### `AnalysisModule` (Interface)
```
Purpose: Contract for analysis modules
Methods:
  - analyze(posts): Map<String, Object>
    └─ Performs analysis, returns results
  - getModuleName(): String
    └─ Module identifier
  - getDescription(): String
    └─ Module description

Return Format:
  Map contains:
    - Charts data
    - Statistics (counts, percentages)
    - Insights & recommendations
    - Summary metrics

Why Interface:
  ✓ Pluggable analysis modules
  ✓ Easy to add new analyses
  ✓ Modular architecture
  ✓ Reusable in different contexts
```

#### `SatisfactionAnalysisModule` (Problem 1)
```
Purpose: Analyze relief category effectiveness
Analysis Steps:

1. Group posts by relief category:
   CASH → [post1, post2, ...]
   MEDICAL → [post3, post4, ...]
   SHELTER → [post5, ...]
   ...

2. For each category, analyze sentiments:
   - Count: positive, negative, neutral
   - Calculate: satisfaction score
   - Determine: effectiveness rating

3. Generate insights:
   - Which categories most effective? (highest satisfaction)
   - Which categories need improvement? (low satisfaction)
   - Resource allocation recommendations

Output Metrics:
  - Category effectiveness scores (1-10)
  - Sentiment distribution per category
  - Satisfaction percentages
  - Detailed insights & recommendations

Use Case:
  ✓ Identify which relief types work best
  ✓ Allocate resources to effective programs
  ✓ Improve low-performing categories
  ✓ Make data-driven decisions
```

#### `TimeSeriesSentimentModule` (Problem 2)
```
Purpose: Track sentiment trends over time
Analysis Steps:

1. Sort posts/comments by timestamp
2. Group by time periods (hour/day/week)
3. For each period, calculate sentiment metrics:
   - Average sentiment score
   - Sentiment type distribution
   - Trend direction (improving/declining)

4. Identify patterns:
   - When was sentiment most positive?
   - When was sentiment most negative?
   - Are trends improving over time?

5. Generate timeline visualization:
   - Line chart of sentiment over time
   - Area chart of sentiment distribution
   - Peak/trough indicators

Use Case:
  ✓ Track impact of relief interventions
  ✓ Identify crisis periods (spike in negative sentiment)
  ✓ Measure program success (improving sentiment)
  ✓ Emergency response timing (when needed most)

Example Output:
  Timeline:
  - Day 1: Sentiment = -0.6 (crisis, very negative)
  - Day 2: Sentiment = -0.3 (improving slightly)
  - Day 3: Sentiment = 0.1 (neutral, aid arriving)
  - Day 4: Sentiment = 0.5 (positive, relief effective)
```

---

### 6. **Database Package** (`com.humanitarian.logistics.database`)
**Responsibility**: Data persistence

**Key Classes**:

#### `DatabaseManager`
```
Purpose: Manage SQLite database connections
Singleton Pattern:
  - Single instance shared across application
  - Thread-safe
  - Lazy initialization

Features:
  - CREATE tables if not exist
  - CRUD operations (Create, Read, Update, Delete)
  - Transaction management
  - Connection pooling
  - WAL (Write-Ahead Logging) support

Tables:
  1. posts
     - post_id (PK)
     - content
     - author
     - source
     - created_at
     - sentiment
     - confidence
     - relief_category
     - disaster_keyword

  2. comments
     - comment_id (PK)
     - post_id (FK → posts)
     - content
     - author
     - created_at
     - sentiment
     - confidence
     - relief_category

Key Methods:
  - savePost(Post): persist post & comments
  - getAllPosts(): load all posts from DB
  - updateComment(Comment): modify comment
  - deleteComment(String): remove comment
  - isDuplicateLink(String): check if post exists

Why Singleton:
  ✓ Only one database connection needed
  ✓ Prevents multiple connections
  ✓ Simplifies resource management
  ✓ Global access point
```

#### `DataPersistenceManager`
```
Purpose: Serialize/deserialize objects for persistence
Features:
  - JSON serialization of posts
  - Load/save disaster types
  - Session persistence (cache)

Why Separate from DatabaseManager:
  ✓ Different responsibility (serialization vs DB)
  ✓ Can use different formats (JSON, XML, etc.)
  ✓ Easier to test independently
```

---

## 🎯 Design Patterns Sử Dụng

### 1. **MVC Pattern** (Model-View-Controller)
```
Model (Model.java)
  - Contains business logic
  - Manages application state (posts, comments)
  - Orchestrates sentiment analysis & classification
  - Notifies observers of changes
  - Does NOT contain UI code

View (View.java + Panels)
  - Displays information
  - Handles layout & rendering
  - Responds to user events
  - Updates based on model changes
  - Contains ONLY UI code

Controller (UI Panels)
  - CrawlControlPanel (handles crawling)
  - DataCollectionPanel (handles data entry)
  - CommentManagementPanel (handles comment operations)
  - AdvancedAnalysisPanel (handles analysis)
  - Each panel orchestrates user interaction

Benefits:
  ✓ Separation of concerns
  ✓ Model testable without UI
  ✓ Easy to test view & controller
  ✓ Reusable model with different UIs
```

### 2. **Observer Pattern** (Listener Model)
```
Subject: Model
  - addModelListener(listener)
  - removeModelListener(listener)
  - notifyListeners()

Observers: View, AdvancedAnalysisPanel
  - implements ModelListener
  - modelChanged() method
  - Gets called when model changes

Flow:
  1. Model data changes
  2. Model calls notifyListeners()
  3. All registered listeners called
  4. View/Panel updates automatically

Benefits:
  ✓ Loose coupling (observers don't know each other)
  ✓ Real-time UI updates
  ✓ Multiple observers possible
  ✓ Easy to add/remove listeners
  ✓ Push-based notification
```

### 3. **Registry Pattern** (Crawler Management)
```
CrawlerRegistry (Singleton)
  - registerCrawler(CrawlerConfig)
  - getCrawlerNames()
  - createCrawler(name): DataCrawler

Usage:
  1. Initialize crawlers once:
     CrawlerManager.initializeCrawlers()
     - YouTube crawler registered
     - Mock crawler registered

  2. Get registry:
     CrawlerRegistry registry = getInstance()

  3. Create crawler dynamically:
     DataCrawler crawler = registry.createCrawler("YOUTUBE")

  4. Use crawler:
     List<Post> posts = crawler.crawlPosts(...)

Benefits:
  ✓ Add crawlers without changing UI code
  ✓ Auto-discovery (UI finds available crawlers)
  ✓ Configuration-driven
  ✓ Supports fallback (try YouTube, fallback to MOCK)
  ✓ Singleton (one registry instance)
```

### 4. **Strategy Pattern** (Pluggable Analysis)
```
Strategy: SentimentAnalyzer interface
Strategies:
  - EnhancedSentimentAnalyzer (fast, keyword-based)
  - PythonSentimentAnalyzer (accurate, ML-based)
  - SimpleSentimentAnalyzer (lightweight, emoji-based)

Context: Model
  - Holds SentimentAnalyzer reference
  - Calls analyzeSentiment() method
  - Doesn't know which strategy

Usage:
  Model model = new Model();
  
  // Use strategy A
  model.setSentimentAnalyzer(new EnhancedSentimentAnalyzer());
  model.addPost(post1);  // Uses Enhanced
  
  // Switch to strategy B
  model.setSentimentAnalyzer(new PythonSentimentAnalyzer());
  model.addPost(post2);  // Uses Python
  
  // No change to Model code!

Benefits:
  ✓ Runtime strategy selection
  ✓ Easy to add new strategies
  ✓ No if-else statements
  ✓ Strategies independent
  ✓ Compare accuracy of different methods
```

### 5. **Factory Pattern** (Crawler Creation)
```
Factory Interface: CrawlerFactory
  - create(): DataCrawler

Implementations:
  - YouTubeCrawler::new (method reference)
  - MockDataCrawler::new (method reference)

CrawlerRegistry
  - Holds map of factories
  - createCrawler() calls factory.create()

Usage:
  CrawlerConfig youtube = new CrawlerConfig(
    "YOUTUBE",
    "YouTube Official API",
    "Crawls YouTube videos",
    YouTubeCrawler::new,  // Factory
    true, true, true
  );
  registry.registerCrawler(youtube);
  
  DataCrawler crawler = registry.createCrawler("YOUTUBE");
  // Creates new YouTubeCrawler instance

Benefits:
  ✓ Encapsulate object creation
  ✓ Deferred creation (lazy)
  ✓ Parameterized creation
  ✓ Easy to add new factories
```

### 6. **Singleton Pattern** (Registry & Database)
```
CrawlerRegistry
  - private static final INSTANCE
  - private constructor
  - public static getInstance()

DatabaseManager
  - private static instance
  - synchronized getInstance()
  - Used throughout application

Benefits:
  ✓ Global access point
  ✓ Only one instance
  ✓ Thread-safe (in DatabaseManager)
  ✓ Lazy initialization
  ✓ Resource management
```

### 7. **Dependency Injection**
```
Constructor Injection:
  View(Model model)
  CrawlControlPanel(Model model)
  DataCollectionPanel(Model model)
  AdvancedAnalysisPanel(Model model)

Injection Points:
  View view = new View(model);
  CrawlControlPanel panel = new CrawlControlPanel(model);
  
  // All components share same model instance
  // Easy to inject mock model for testing

Benefits:
  ✓ Testability (inject mock Model)
  ✓ Flexibility (swap implementations)
  ✓ Loose coupling
  ✓ Easy to understand dependencies
```

---

## 🔄 Flow & Interactions

### Crawling Flow
```
1. User Input
   └─ CrawlControlPanel.startCrawling()
      └─ Get keywords from UI text area
      └─ Get limit from spinner
      └─ Get crawler type from combo box

2. Get Crawler
   └─ CrawlerRegistry.createCrawler(name)
      └─ Look up crawler factory
      └─ Create new crawler instance

3. Crawl Data
   └─ DataCrawler.crawlPosts()
      └─ YouTube: scrape web page, extract videos & comments
      └─ Mock: generate random test data
      └─ Return: List<Post>

4. Process Results
   └─ CrawlingUtility.processAndAddPosts()
      └─ Check for duplicates
      └─ Add comments if needed
      └─ Find disaster type from keywords
      └─ Add to Model

5. Model Processing
   └─ Model.addPost()
      └─ Analyze sentiment (SentimentAnalyzer)
      └─ Classify category (PythonCategoryClassifier)
      └─ Save to database (DatabaseManager)
      └─ Notify listeners

6. UI Update
   └─ View.modelChanged()
      └─ Update status label
      └─ Refresh post count
   └─ AdvancedAnalysisPanel.modelChanged()
      └─ Refresh analysis charts
   └─ CommentManagementPanel.refreshTable()
      └─ Update comment table
```

### Analysis Flow
```
1. User Clicks "Analyze"
   └─ AdvancedAnalysisPanel.updateProblem1Analysis()

2. Get Model Data
   └─ Model.getPosts()
      └─ Returns List<Post> with comments

3. Perform Analysis
   └─ Model.performAnalysis("satisfaction")
      └─ Get AnalysisModule from map
      └─ Call module.analyze(posts)

4. Analysis Module
   └─ SatisfactionAnalysisModule.analyze()
      └─ Group posts by relief category
      └─ Calculate sentiment statistics per category
      └─ Generate effectiveness scores
      └─ Generate recommendations
      └─ Return Map<String, Object>

5. Render Results
   └─ AdvancedAnalysisPanel
      └─ Extract results from map
      └─ Create charts (ChartsUtility)
      └─ Display charts in ChartPanel
      └─ Show text results in JTextArea
      └─ Show recommendations

6. Display to User
   └─ User sees:
      └─ Charts showing satisfaction by category
      └─ Statistics & percentages
      └─ Detailed insights
      └─ Resource allocation recommendations
```

---

## 💾 Technology Stack

### Frontend
- **Framework**: Java Swing
  - JFrame (main window)
  - JPanel (layout panels)
  - JTabbedPane (tabbed interface)
  - JTable (data grid)
  - JTextArea, JTextField (text input)
  - JButton, JComboBox, JSpinner (controls)

- **Charts**: JFreeChart
  - BarChart (sentiment distribution)
  - PieChart (category breakdown)
  - LineChart (time series trends)
  - StackedBarChart (multi-series data)

### Backend / Business Logic
- **Language**: Java 11+
- **Architecture**: MVC + Design Patterns
- **Concurrency**: Threading for long-running operations
- **Logging**: java.util.logging

### Data Layer
- **Database**: SQLite
  - Lightweight, file-based
  - No server required
  - ACID transactions
  - WAL support for concurrent access

- **JDBC**: Direct SQL execution
  - Connection pooling
  - Prepared statements

### External Services
- **Web Scraping**: Jsoup
  - HTML parsing
  - CSS selectors
  - DOM traversal

- **HTTP Client**: OkHttp
  - HTTP requests
  - Connection management
  - Retry logic

- **JSON Processing**: GSON
  - JSON serialization
  - Object mapping

- **NLP/ML**:
  - Python backend (localhost:5001)
  - xlm-roberta model
  - Zero-shot classification
  - Sentiment analysis & category classification

### Development
- **Build Tool**: Maven
- **Testing**: JUnit (implied, not shown in code)
- **Version Control**: Git

---

## 🏛️ System Architecture Summary

### Layered Architecture
```
┌─────────────────────────────┐
│  Presentation Layer (UI)    │  ← User Interaction
├─────────────────────────────┤
│  Business Logic Layer       │  ← Core Application Logic
│  (Model, Analysis, Crawlers)│
├─────────────────────────────┤
│  Data Access Layer          │  ← Persistence
│  (Database, Models)         │
├─────────────────────────────┤
│  External Services Layer    │  ← External Resources
│  (Python, APIs)             │
└─────────────────────────────┘
```

### Key Design Principles Used
1. **Separation of Concerns**
   - UI (View) separate from Business Logic (Model)
   - Each class has single responsibility

2. **Dependency Inversion**
   - Depend on interfaces, not implementations
   - Crawlers implement DataCrawler interface
   - Analyzers implement SentimentAnalyzer interface

3. **Open/Closed Principle**
   - Open for extension (add crawlers, analyzers)
   - Closed for modification (don't change existing code)
   - Registry pattern enables this

4. **DRY (Don't Repeat Yourself)**
   - Shared utility methods (CrawlingUtility, ChartsUtility)
   - Base classes (Post abstract)
   - Reusable interfaces

5. **YAGNI (You Aren't Gonna Need It)**
   - Code focused on Problem 1 & 2 requirements
   - No unnecessary abstractions

---

## 📊 Scalability & Extensibility

### Easy to Add
```
1. New Crawler:
   - Implement DataCrawler interface
   - Register in CrawlerManager
   - Auto-appears in UI combo box

2. New Sentiment Analyzer:
   - Implement SentimentAnalyzer interface
   - Set via model.setSentimentAnalyzer()
   - Model automatically uses it

3. New Analysis Module:
   - Implement AnalysisModule interface
   - Register in Model
   - Appears in analysis tab

4. New Relief Category:
   - Add to ReliefItem.Category enum
   - Automatically supported throughout
```

### Easy to Test
```
1. Model can be tested independently
   - No UI required
   - Inject mock SentimentAnalyzer
   - Verify analysis results

2. UI components testable
   - Create Model with test data
   - Verify UI updates

3. Crawlers testable
   - Create post lists
   - Verify data extraction
   - No actual web access needed

4. Analysis modules testable
   - Create synthetic post lists
   - Verify analysis output
```

---

## 🎓 Summary

### This Architecture Provides:
- ✅ **Clear Separation** of concerns (MVC)
- ✅ **Flexibility** to add crawlers, analyzers, modules
- ✅ **Testability** at each layer
- ✅ **Maintainability** with design patterns
- ✅ **Scalability** to handle more data & users
- ✅ **Extensibility** for new features
- ✅ **Performance** with threading & caching
- ✅ **Reliability** with error handling & fallbacks

### Key Strengths:
1. **Pluggable Components** - Easy to swap implementations
2. **Observer Pattern** - Real-time UI updates
3. **Registry Pattern** - Dynamic component management
4. **Singleton Pattern** - Resource management
5. **Dependency Injection** - Testability & flexibility
6. **Interface Segregation** - Focused contracts

### When to Use This Architecture:
- ✅ Projects with multiple data sources
- ✅ Projects with pluggable components
- ✅ GUI applications needing MVC
- ✅ Systems requiring real-time updates
- ✅ Testable, maintainable codebases

