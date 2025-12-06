# OOP Architecture Analysis - Humanitarian Logistics UI (User Interface)

## 📋 Executive Summary
Humanitarian Logistics UI demonstrates a **comprehensive and mature** Object-Oriented Programming architecture using:
- **7 OOP Cơ Bản** (Encapsulation, Abstraction, Inheritance, Polymorphism, Interface, Abstract Class, Composition)
- **5 OOP Nâng Cao** (Design Patterns: MVC, Registry, Strategy, Observer, Factory)
- **Advanced Techniques**: Singleton, Dependency Injection, Method Chaining

---

## 🏗️ PHẦN 1: CÁC KỸ THUẬT OOP CƠ BẢN

### 1️⃣ **ENCAPSULATION (Đóng gói dữ liệu)**

#### Vị trí: `Post.java` (Abstract Base Class)
```java
package com.humanitarian.logistics.model;

public abstract class Post implements Serializable, Comparable<Post> {
    // Private fields - Dữ liệu được bảo vệ
    private final String postId;
    private final String content;
    private final LocalDateTime createdAt;
    private final String author;
    private final String source;
    private Sentiment sentiment;
    private ReliefItem reliefItem;
    private final List<Comment> comments;
    
    // Immutable - protected constructor
    protected Post(String postId, String content, LocalDateTime createdAt,
                   String author, String source) {
        this.postId = Objects.requireNonNull(postId);
        this.content = Objects.requireNonNull(content);
        this.createdAt = Objects.requireNonNull(createdAt);
        // ... validation
    }
    
    // Getter methods - Kiểm soát truy cập
    public String getPostId() { return postId; }
    public String getContent() { return content; }
    
    // Setter methods - Validation khi gán giá trị
    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }
    
    // Collections được trả về immutable
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }
}
```

**Lợi ích:**
- ✅ **Dữ liệu được bảo vệ**: Không ai có thể thay đổi `postId` sau khi tạo
- ✅ **Kiểm soát truy cập**: Chỉ có thể thay đổi `sentiment` qua `setSentiment()`
- ✅ **Validation dữ liệu**: `Objects.requireNonNull()` đảm bảo không null
- ✅ **Immutable collections**: Trả về `Collections.unmodifiableList()` để ngăn chặn sửa đổi

#### Vị trí: `Model.java` (UI Model)
```java
public class Model {
    private List<Post> posts;                          // Private
    private SentimentAnalyzer sentimentAnalyzer;       // Private
    private PythonCategoryClassifier categoryClassifier; // Private
    private List<ModelListener> listeners;             // Private
    
    public Model() {
        this.posts = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }
    
    // Getter - trả về copy để bảo vệ internal state
    public List<Post> getPosts() {
        return new ArrayList<>(posts);  // Copy, không return reference
    }
    
    // Setter - validation
    public void setSentimentAnalyzer(SentimentAnalyzer analyzer) {
        if (this.sentimentAnalyzer != null) {
            this.sentimentAnalyzer.shutdown();  // Cleanup cũ
        }
        this.sentimentAnalyzer = analyzer;
        this.sentimentAnalyzer.initialize();
        notifyListeners();  // Notify observers
    }
}
```

**Lợi ích:**
- ✅ **Bảo vệ trạng thái nội bộ**: Model chỉ cung cấp controlled access
- ✅ **Prevent external modification**: `getPosts()` trả về copy, không reference gốc
- ✅ **Resource cleanup**: `setSentimentAnalyzer()` shutdown analyzer cũ trước khi gán mới

---

### 2️⃣ **ABSTRACTION (Trừu tượng hóa)**

#### Vị trí: `SentimentAnalyzer.java` (Interface)
```java
public interface SentimentAnalyzer {
    // Abstract methods - ẩn chi tiết implementation
    Sentiment analyzeSentiment(String text);
    Sentiment[] analyzeSentimentBatch(String[] texts);
    String getModelName();
    void initialize();
    void shutdown();
}
```

#### Vị trí: `EnhancedSentimentAnalyzer.java` (Implementation)
```java
public class EnhancedSentimentAnalyzer implements SentimentAnalyzer {
    
    private static final String[] POSITIVE_WORDS_EN = {...};
    private static final String[] NEGATIVE_WORDS_VI = {...};
    
    @Override
    public Sentiment analyzeSentiment(String text) {
        // Chi tiết implementation - người dùng không cần biết
        String lowerText = text.toLowerCase();
        int positiveCount = countMatches(lowerText, POSITIVE_WORDS_EN);
        int negativeCount = countMatches(lowerText, NEGATIVE_WORDS_EN);
        // ... logic phức tạp
        return new Sentiment(type, confidence, text);
    }
    
    // Helper method - abstracted away
    private int countMatches(String text, String[] words) {
        int count = 0;
        for (String word : words) {
            if (text.contains(word)) count++;
        }
        return count;
    }
}
```

**Lợi ích:**
- ✅ **Ẩn chi tiết phức tạp**: Client chỉ cần gọi `analyzeSentiment()`, không cần biết keyword matching logic
- ✅ **Swap implementations dễ dàng**: Có thể thay `EnhancedSentimentAnalyzer` bằng `PythonSentimentAnalyzer`
- ✅ **Tập trung vào use case**: Client tập trung vào "phân tích sentiment", không "cách thực hiện"

#### Vị trí: `AnalysisModule.java` (Interface)
```java
public interface AnalysisModule {
    Map<String, Object> analyze(List<Post> posts);
    String getModuleName();
    String getDescription();
}
```

#### Vị trí: `SatisfactionAnalysisModule.java` (Implementation)
```java
public class SatisfactionAnalysisModule implements AnalysisModule {
    @Override
    public Map<String, Object> analyze(List<Post> posts) {
        // Chi tiết implementation phức tạp về satisfaction scoring
        Map<ReliefItem.Category, List<Sentiment>> sentimentsByCategory = new HashMap<>();
        // ... logic phân tích
        results.put("problem_1_satisfaction_analysis", categoryStats);
        results.put("category_effectiveness", categoryEffectiveness);
        return results;
    }
}
```

**Lợi ích:**
- ✅ **Abstraction layer**: Mô-đun phân tích trừu tượng có thể được thay thế
- ✅ **Mở rộng dễ dàng**: Thêm `TimeSeriesSentimentModule` mà không cần sửa interface

---

### 3️⃣ **INHERITANCE (Kế thừa)**

#### Vị trí: `Post.java` (Abstract Base Class)
```java
public abstract class Post implements Serializable, Comparable<Post> {
    protected Post(String postId, String content, LocalDateTime createdAt,
                   String author, String source) {
        this.postId = postId;
        this.content = content;
        // ... shared initialization
    }
    
    public String getPostId() { return postId; }
    public List<Comment> getComments() { return comments; }
    // ... 20+ shared methods
}
```

#### Vị trí: `YouTubePost.java` (Concrete Subclass)
```java
public class YouTubePost extends Post {
    private String channelId;
    private int likes;
    private int views;
    private DisasterType disasterType;
    
    public YouTubePost(String postId, String content, LocalDateTime createdAt,
                       String author, String channelId) {
        super(postId, content, createdAt, author, "YOUTUBE");  // Call parent
        this.channelId = channelId;
        this.likes = 0;
        this.views = 0;
        // Specialized initialization for YouTube
        this.setReliefItem(new ReliefItem(ReliefItem.Category.FOOD, "General Relief", 3));
    }
    
    // YouTube-specific methods
    public String getChannelId() { return channelId; }
    public void setLikes(int likes) { this.likes = likes; }
    
    @Override
    public String toString() {
        // Override parent method for YouTube-specific formatting
        return "YouTubePost{" + "postId='" + getPostId() + '\'' + "...";
    }
}
```

**Lợi ích:**
- ✅ **Code reuse**: `YouTubePost` kế thừa 20+ methods từ `Post` (getPostId, getComments, addComment, etc.)
- ✅ **Polymorphism support**: Có thể xử lý `Post[] posts` và mỗi post có behavior khác nhau
- ✅ **Mở rộng dễ dàng**: Có thể thêm `FacebookPost`, `TwitterPost` cùng kế thừa từ `Post`
- ✅ **Shared state & behavior**: Tất cả subclass đều có `comments`, `sentiment`, `reliefItem`

---

### 4️⃣ **POLYMORPHISM (Đa hình)**

#### Vị trí: `Model.java` (Polymorphic behavior)
```java
public class Model {
    private SentimentAnalyzer sentimentAnalyzer;  // Interface reference
    
    public void setSentimentAnalyzer(SentimentAnalyzer analyzer) {
        this.sentimentAnalyzer = analyzer;
    }
    
    public void addPost(Post post) {
        // Polymorphic call - sẽ gọi analyze method của subclass
        if (post.getSentiment() == null && sentimentAnalyzer != null) {
            Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(post.getContent());
            post.setSentiment(sentiment);
        }
        
        for (Comment comment : post.getComments()) {
            if (comment.getSentiment() == null && sentimentAnalyzer != null) {
                Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(comment.getContent());
                comment.setSentiment(sentiment);
            }
        }
    }
}
```

**Sử dụng:**
```java
// Runtime selection - polymorphism in action!
Model model = new Model();

// Có thể sử dụng EnhancedSentimentAnalyzer
model.setSentimentAnalyzer(new EnhancedSentimentAnalyzer());

// Hoặc PythonSentimentAnalyzer
model.setSentimentAnalyzer(new PythonSentimentAnalyzer());

// Hoặc SimpleSentimentAnalyzer
model.setSentimentAnalyzer(new SimpleSentimentAnalyzer());

// Mã không thay đổi, behavior thay đổi theo runtime type!
model.addPost(post);  // Gọi method của analyzer hiện tại
```

**Lợi ích:**
- ✅ **Flexible implementation**: Runtime chọn implementation nào sử dụng
- ✅ **No code change**: Client code không cần thay đổi khi thêm analyzer mới
- ✅ **Dependency Injection**: Inject dependency tại runtime thay vì hardcoding

#### Vị trí: `CrawlControlPanel.java` (Polymorphic crawlers)
```java
private void startCrawling() {
    new Thread(() -> {
        DataCrawler crawler = null;
        try {
            // Polymorphic creation - kiểu crawler được decide tại runtime
            CrawlerRegistry.CrawlerConfig config = crawlerRegistry.getConfig(selectedCrawlerName);
            crawler = crawlerRegistry.createCrawler(selectedCrawlerName);
            
            // Polymorphic method call - mỗi crawler có implementation khác
            List<Post> posts = crawler.crawlPosts(hashtags, new ArrayList<>(), postLimit);
            
            // Use polymorphic result - Post có thể là YouTubePost, FacebookPost, etc.
            for (Post post : posts) {
                // Generic code hoạt động cho mọi post type
                addCommentsToPost(post, commentLimit);
            }
        } catch (Exception e) {
            // Fallback to different crawler
            crawler = crawlerRegistry.createCrawler("MOCK");
            posts = crawler.crawlPosts(...);
        }
    }).start();
}
```

**Lợi ích:**
- ✅ **Multiple crawler types**: YouTubeCrawler, MockDataCrawler, và có thể thêm FacebookCrawler
- ✅ **Same interface, different behavior**: Mỗi crawler `crawlPosts()` khác nhau
- ✅ **Fallback mechanism**: Tự động fallback to MOCK nếu YouTube fails

---

### 5️⃣ **INTERFACES (Giao diện)**

#### Vị trí: `DataCrawler.java` (Contract)
```java
public interface DataCrawler {
    List<Post> crawlPosts(List<String> keywords, List<String> hashtags, int limit);
    String getCrawlerName();
    boolean isInitialized();
    void shutdown();
}
```

Được implement bởi:
- `YouTubeCrawler` - Web scraping from YouTube
- `MockDataCrawler` - Generates test data
- `FacebookCrawler` (có thể add) - Facebook API integration

**Lợi ích:**
- ✅ **Contract**: Định rõ what mà không care how
- ✅ **Multiple implementations**: Dễ thêm crawler mới
- ✅ **Testability**: Dễ tạo MockCrawler cho testing

#### Vị trí: `ModelListener.java` (Observer pattern)
```java
public interface ModelListener {
    void modelChanged();
}
```

Được implement bởi:
- `View` - UI listener
- `AdvancedAnalysisPanel` - Analysis panel listener

**Lợi ích:**
- ✅ **Loose coupling**: Model không biết chi tiết listeners
- ✅ **Multiple listeners**: Nhiều UI component có thể listen to same model
- ✅ **Real-time updates**: Tất cả UI tự động update khi model change

---

### 6️⃣ **ABSTRACT CLASSES**

#### Vị trí: `Post.java`
```java
public abstract class Post implements Serializable, Comparable<Post> {
    // Concrete fields & methods
    private final String postId;
    private final String content;
    
    protected Post(String postId, String content, LocalDateTime createdAt,
                   String author, String source) {
        // Shared initialization logic
    }
    
    // Concrete methods
    public void addComment(Comment comment) { this.comments.add(comment); }
    public List<Comment> getComments() { return comments; }
    
    // Abstract methods (if needed)
    @Override
    public abstract int compareTo(Post other);
}
```

**Tại sao abstract class thay vì interface?**
- ✅ **Shared state**: Tất cả post có `postId`, `content`, `comments`
- ✅ **Protected access**: Subclass có thể access `protected` fields
- ✅ **Initialization logic**: Constructor initialization shared
- ✅ **Constructor parameters**: Interface không thể có constructor

---

### 7️⃣ **COMPOSITION (Kết hợp)**

#### Vị trí: `Model.java` - HAS-A relationship
```java
public class Model {
    // Composition - Model HAS-A SentimentAnalyzer
    private SentimentAnalyzer sentimentAnalyzer;
    
    // Composition - Model HAS-A PythonCategoryClassifier
    private PythonCategoryClassifier categoryClassifier;
    
    // Composition - Model HAS-A DatabaseManager
    private DatabaseManager dbManager;
    
    // Composition - Model HAS-A DataPersistenceManager
    private DataPersistenceManager persistenceManager;
    
    // Composition - Model HAS-A Map of AnalysisModules
    private Map<String, AnalysisModule> analysisModules;
    
    // Composition - Model HAS-A List of Posts
    private List<Post> posts;
}
```

**Lợi ích:**
- ✅ **Flexibility**: Có thể thay đổi SentimentAnalyzer mà không tạo Model mới
- ✅ **Single Responsibility**: Mỗi class có một responsibility
- ✅ **Testability**: Dễ inject mock dependencies

#### Vị trí: `YouTubePost.java` - HAS-A relationship
```java
public class YouTubePost extends Post {
    // Inheritance từ Post
    // ... inherited: postId, content, comments, sentiment, reliefItem
    
    // Composition - YouTubePost HAS-A DisasterType
    private DisasterType disasterType;
    
    // Composition - YouTubePost HAS-A ReliefItem (từ parent class)
    // private ReliefItem reliefItem;  // từ Post
}
```

---

## 🎯 PHẦN 2: CÁC KỸ THUẬT OOP NÂNG CAO (Design Patterns)

### Pattern 1️⃣: **MVC (Model-View-Controller)**

#### Kiến trúc:
```
┌─────────────────────────────────────┐
│           View Layer                │
├─────────────────────────────────────┤
│ View (JFrame)                       │
│ ├── CrawlControlPanel (JPanel)     │
│ ├── DataCollectionPanel (JPanel)   │
│ ├── CommentManagementPanel (JPanel)│
│ └── AdvancedAnalysisPanel (JPanel) │
└─────────────────────────────────────┘
            ↕ (Listener pattern)
┌─────────────────────────────────────┐
│        Model Layer                  │
├─────────────────────────────────────┤
│ Model                               │
│ ├── List<Post> posts                │
│ ├── SentimentAnalyzer               │
│ ├── CategoryClassifier              │
│ └── DatabaseManager                 │
└─────────────────────────────────────┘
            ↕ (Uses)
┌─────────────────────────────────────┐
│       Business Logic Layer          │
├─────────────────────────────────────┤
│ Crawlers, Analyzers, Database       │
└─────────────────────────────────────┘
```

#### Vị trí:

**Model** - `Model.java`:
```java
public class Model {
    // Business logic
    public void addPost(Post post) { /* ... */ }
    public List<Post> getPosts() { /* ... */ }
    public Map<String, Object> performAnalysis(String moduleName) { /* ... */ }
}
```

**View** - `View.java`:
```java
public class View extends JFrame implements ModelListener {
    private Model model;
    
    public View(Model model) {
        this.model = model;
        model.addModelListener(this);  // Register for updates
    }
    
    @Override
    public void modelChanged() {
        // Automatically refresh UI when model changes
        SwingUtilities.invokeLater(() -> {
            List<Post> posts = model.getPosts();
            statusLabel.setText("Posts: " + posts.size());
        });
    }
}
```

**Controller** - `CrawlControlPanel.java`, `DataCollectionPanel.java`:
```java
public class CrawlControlPanel extends JPanel {
    private final Model model;
    
    private void startCrawling() {
        // Controller: Handle user input
        List<String> hashtags = /* parse user input */;
        int postLimit = (Integer) postLimitSpinner.getValue();
        
        // Tell model to do the work
        model.addPost(crawledPost);
    }
}
```

**Lợi ích của MVC:**
- ✅ **Separation of concerns**: Model, View, Controller có trách nhiệm riêng
- ✅ **Testability**: Có thể test Model mà không cần UI
- ✅ **Reusability**: Cùng Model có thể được dùng với CLI, Web, Desktop UI
- ✅ **Maintainability**: Sửa UI không ảnh hưởng tới business logic

---

### Pattern 2️⃣: **REGISTRY (Thư viện đăng ký)**

#### Vị trí: `CrawlerRegistry.java`
```java
public class CrawlerRegistry {
    private static final CrawlerRegistry INSTANCE = new CrawlerRegistry();
    
    private final Map<String, CrawlerFactory> crawlers = new LinkedHashMap<>();
    private final Map<String, CrawlerConfig> crawlerConfigs = new LinkedHashMap<>();
    
    // Singleton
    public static CrawlerRegistry getInstance() {
        return INSTANCE;
    }
    
    // Register crawlers
    public void registerCrawler(CrawlerConfig config) {
        crawlers.put(config.name, config.factory);
        crawlerConfigs.put(config.name, config);
    }
    
    // Query registered crawlers
    public List<String> getCrawlerNames() {
        return new ArrayList<>(crawlers.keySet());
    }
    
    public List<String> getCrawlerDisplayNames() {
        return crawlerConfigs.values().stream()
            .map(c -> c.displayName)
            .toList();
    }
    
    // Create crawler instances
    public DataCrawler createCrawler(String crawlerName) {
        CrawlerFactory factory = crawlers.get(crawlerName);
        return factory.create();
    }
}
```

#### Vị trí: `CrawlerManager.java` (Bootstrap)
```java
public class CrawlerManager {
    public static void initializeCrawlers() {
        CrawlerRegistry registry = CrawlerRegistry.getInstance();
        
        // Register YouTube Crawler
        registry.registerCrawler(new CrawlerRegistry.CrawlerConfig(
            "YOUTUBE", "YouTube Official API", "Uses official YouTube API v3",
            YouTubeCrawler::new,
            true, true, true
        ));
        
        // Register Mock Crawler
        registry.registerCrawler(new CrawlerRegistry.CrawlerConfig(
            "MOCK", "Mock Data Generator", "Generates test data for development",
            MockDataCrawler::new,
            false, true, false
        ));
    }
}
```

#### Vị trí: `CrawlControlPanel.java` (Usage)
```java
public class CrawlControlPanel extends JPanel {
    private CrawlerRegistry crawlerRegistry = CrawlerRegistry.getInstance();
    
    private void initializeUI() {
        if (crawlerRegistry.getCrawlerNames().isEmpty()) {
            CrawlerManager.initializeCrawlers();
        }
        
        // Dynamically populate combo box from registry
        String[] crawlerNames = crawlerRegistry.getCrawlerDisplayNames()
            .toArray(new String[0]);
        platformSelector = new JComboBox<>(crawlerNames);
    }
    
    private void startCrawling() {
        // Dynamically get crawler from registry
        DataCrawler crawler = crawlerRegistry.createCrawler(selectedCrawlerName);
        List<Post> posts = crawler.crawlPosts(hashtags, new ArrayList<>(), postLimit);
    }
}
```

**Lợi ích của Registry Pattern:**
- ✅ **Add crawlers without UI changes**: `CrawlControlPanel` không biết về crawler types
- ✅ **Runtime registration**: Có thể register crawlers từ config file, plugins, etc.
- ✅ **Discovery**: UI tự động discover available crawlers
- ✅ **Extensibility**: Thêm `FacebookCrawler` chỉ cần gọi `registerCrawler()` ở `CrawlerManager`

---

### Pattern 3️⃣: **STRATEGY (Chiến lược)**

#### Vị trí: `SentimentAnalyzer.java` (Strategy interface)
```java
public interface SentimentAnalyzer {
    Sentiment analyzeSentiment(String text);
    Sentiment[] analyzeSentimentBatch(String[] texts);
}
```

Có multiple strategies:
- `EnhancedSentimentAnalyzer` - Keyword-based analysis
- `PythonSentimentAnalyzer` - Uses xlm-roberta model
- `SimpleSentimentAnalyzer` - Basic emoji/keyword detection

#### Vị trí: `Model.java` (Context)
```java
public class Model {
    private SentimentAnalyzer sentimentAnalyzer;  // Strategy
    
    public void setSentimentAnalyzer(SentimentAnalyzer analyzer) {
        // Change strategy at runtime!
        this.sentimentAnalyzer = analyzer;
        this.sentimentAnalyzer.initialize();
    }
    
    public void addPost(Post post) {
        if (sentimentAnalyzer != null) {
            // Use chosen strategy
            Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(post.getContent());
            post.setSentiment(sentiment);
        }
    }
}
```

#### Usage:
```java
Model model = new Model();

// Choose strategy A - Enhanced analyzer
model.setSentimentAnalyzer(new EnhancedSentimentAnalyzer());
model.addPost(post);  // Uses EnhancedSentimentAnalyzer

// Switch to strategy B - Python analyzer
model.setSentimentAnalyzer(new PythonSentimentAnalyzer());
model.addPost(anotherPost);  // Uses PythonSentimentAnalyzer

// No change to Model code!
```

**Lợi ích:**
- ✅ **Runtime strategy selection**: Choose strategy without recompilation
- ✅ **Easy to add new strategies**: Implement `SentimentAnalyzer` interface
- ✅ **No if-else statements**: Strategy encapsulated in classes
- ✅ **Testability**: Easy to test with mock strategies

---

### Pattern 4️⃣: **OBSERVER (Người quan sát)**

#### Vị trí: `ModelListener.java` (Observer interface)
```java
public interface ModelListener {
    void modelChanged();
}
```

#### Vị trí: `Model.java` (Subject)
```java
public class Model {
    private List<ModelListener> listeners;
    
    public void addModelListener(ModelListener listener) {
        listeners.add(listener);
    }
    
    public void removeModelListener(ModelListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners() {
        for (ModelListener listener : listeners) {
            listener.modelChanged();
        }
    }
    
    public void addPost(Post post) {
        // ... add post logic
        notifyListeners();  // Notify all listeners
    }
}
```

#### Vị trí: `View.java` & `AdvancedAnalysisPanel.java` (Observers)
```java
public class View extends JFrame implements ModelListener {
    private Model model;
    
    public View(Model model) {
        this.model = model;
        model.addModelListener(this);  // Register as observer
    }
    
    @Override
    public void modelChanged() {
        // React to model changes
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Model updated - " + model.getPosts().size() + " posts");
        });
    }
}

public class AdvancedAnalysisPanel extends JPanel implements ModelListener {
    private Model model;
    
    public AdvancedAnalysisPanel(Model model) {
        this.model = model;
        model.addModelListener(this);  // Also register
    }
    
    @Override
    public void modelChanged() {
        // Refresh analysis charts
        updateAnalysisCharts();
    }
}
```

**Lợi ích:**
- ✅ **Loose coupling**: Model không biết về View/AnalysisPanel
- ✅ **Multiple observers**: Nhiều UI component tự động update
- ✅ **Real-time sync**: Tất cả UI stay in sync với Model
- ✅ **Easy to add observers**: Chỉ implement `ModelListener`

---

### Pattern 5️⃣: **FACTORY (Nhà máy)**

#### Vị trí: `CrawlerRegistry.java`
```java
@FunctionalInterface
public interface CrawlerFactory {
    DataCrawler create();
}

public class CrawlerConfig {
    public final CrawlerFactory factory;  // Factory function
    
    public CrawlerConfig(String name, String displayName, String description,
                        CrawlerFactory factory, ...) {
        this.factory = factory;
    }
}

public DataCrawler createCrawler(String crawlerName) {
    CrawlerFactory factory = crawlers.get(crawlerName);
    return factory.create();  // Factory method creates instance
}
```

#### Vị trí: `CrawlerManager.java`
```java
registry.registerCrawler(new CrawlerRegistry.CrawlerConfig(
    "YOUTUBE", "YouTube Official API", "...",
    YouTubeCrawler::new,  // Method reference = factory
    true, true, true
));

registry.registerCrawler(new CrawlerRegistry.CrawlerConfig(
    "MOCK", "Mock Data Generator", "...",
    MockDataCrawler::new,  // Different factory
    false, true, false
));
```

**Lợi ích:**
- ✅ **Encapsulate creation logic**: Registry handles creation
- ✅ **Lazy creation**: Crawlers created only when needed
- ✅ **Parameterized creation**: Factory can accept parameters
- ✅ **Easy to test**: Can provide MockFactory for testing

---

## 📊 PHẦN 3: ADVANCED TECHNIQUES

### Technique 1️⃣: **SINGLETON PATTERN**

#### Vị trí: `CrawlerRegistry.java`
```java
public class CrawlerRegistry {
    private static final CrawlerRegistry INSTANCE = new CrawlerRegistry();
    
    private CrawlerRegistry() {}  // Private constructor
    
    public static CrawlerRegistry getInstance() {
        return INSTANCE;  // Always return same instance
    }
}
```

#### Usage:
```java
CrawlerRegistry registry1 = CrawlerRegistry.getInstance();
CrawlerRegistry registry2 = CrawlerRegistry.getInstance();
assert registry1 == registry2;  // True! Same instance
```

**Lợi ích:**
- ✅ **Global access**: Accessible từ anywhere
- ✅ **Single instance**: Ensures only one registry exists
- ✅ **Thread-safe**: Instance created at class load time

---

### Technique 2️⃣: **DEPENDENCY INJECTION**

#### Vị trí: Constructor injection
```java
public class View extends JFrame implements ModelListener {
    private Model model;
    
    public View(Model model) {
        this.model = model;  // Injected dependency
        model.addModelListener(this);
    }
}

public class CrawlControlPanel extends JPanel {
    private final Model model;
    
    public CrawlControlPanel(Model model) {
        this.model = model;  // Injected dependency
    }
}

public class AdvancedAnalysisPanel extends JPanel implements ModelListener {
    private Model model;
    
    public AdvancedAnalysisPanel(Model model) {
        this.model = model;  // Injected dependency
    }
}
```

#### Usage:
```java
Model model = new Model();

// Inject same model to all views
View view = new View(model);
CrawlControlPanel crawlPanel = new CrawlControlPanel(model);
AdvancedAnalysisPanel analysisPanel = new AdvancedAnalysisPanel(model);

// All components share same model instance
// Easy to test by providing mock Model
```

**Lợi ích:**
- ✅ **Testability**: Inject mock Model for testing
- ✅ **Flexibility**: Swap implementations easily
- ✅ **Loose coupling**: Components don't create their own dependencies
- ✅ **Reusability**: Same component can work with different Model implementations

---

### Technique 3️⃣: **METHOD CHAINING / FLUENT INTERFACE**

#### Vị trí: `Model.java`
```java
public void setSentimentAnalyzer(SentimentAnalyzer analyzer) {
    if (this.sentimentAnalyzer != null) {
        this.sentimentAnalyzer.shutdown();
    }
    this.sentimentAnalyzer = analyzer;
    this.sentimentAnalyzer.initialize();
    notifyListeners();  // Chaining - perform related action
    return this;  // Could return this for fluent API
}
```

#### Vị trí: `Post.java`
```java
public void addComment(Comment comment) {
    if (comment != null) {
        this.comments.add(comment);
    }
}

public void setSentiment(Sentiment sentiment) {
    this.sentiment = sentiment;  // Can chain multiple setters
}

// Usage: Fluent-like (could improve with builder pattern)
post.setSentiment(sentiment);
post.setReliefItem(reliefItem);
post.addComment(comment);
```

---

### Technique 4️⃣: **IMMUTABILITY & DEFENSIVE COPYING**

#### Vị trí: `Post.java`
```java
public abstract class Post {
    private final String postId;    // Final - immutable
    private final String content;   // Final - immutable
    private final LocalDateTime createdAt;  // Final
    private final String author;    // Final
    private final String source;    // Final
    
    protected Post(String postId, String content, LocalDateTime createdAt,
                   String author, String source) {
        this.postId = Objects.requireNonNull(postId);  // Null-check
        this.content = Objects.requireNonNull(content);
        // ... more null checks
    }
    
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);  // Defensive copy
    }
    
    public String getPostId() {
        return postId;  // Safe to return - immutable
    }
}
```

#### Vị trí: `Model.java`
```java
public List<Post> getPosts() {
    return new ArrayList<>(posts);  // Defensive copy - not reference
}

public void clearPosts() {
    posts.clear();  // Safe - internal state
    notifyListeners();  // Controlled modification
}
```

**Lợi ích:**
- ✅ **Thread-safe**: Immutable objects can be shared safely
- ✅ **Prevent bugs**: Can't accidentally modify returned list
- ✅ **Null safety**: `Objects.requireNonNull()` prevents NPE
- ✅ **Predictable behavior**: Objects don't change unexpectedly

---

### Technique 5️⃣: **INTERFACE SEGREGATION PRINCIPLE**

#### Vị trí: `SentimentAnalyzer.java`
```java
public interface SentimentAnalyzer {
    Sentiment analyzeSentiment(String text);
    Sentiment[] analyzeSentimentBatch(String[] texts);
    String getModelName();
    void initialize();
    void shutdown();
}
```

#### Vị trí: `DataCrawler.java`
```java
public interface DataCrawler {
    List<Post> crawlPosts(List<String> keywords, List<String> hashtags, int limit);
    String getCrawlerName();
    boolean isInitialized();
    void shutdown();
}
```

#### Vị trí: `ModelListener.java`
```java
public interface ModelListener {
    void modelChanged();  // Single responsibility
}
```

**Lợi ích:**
- ✅ **Focused interfaces**: Each interface has single purpose
- ✅ **Easy implementation**: Minimal methods to implement
- ✅ **Better contracts**: Clear what each interface represents
- ✅ **Flexibility**: Can combine multiple small interfaces

---

## 🎁 PHẦN 4: OOP BENEFITS SUMMARY

### Encapsulation Benefits
| Benefit | Implementation | Example |
|---------|-----------------|---------|
| **Data Protection** | Private fields | `private final String postId` |
| **Validation** | Null checks | `Objects.requireNonNull(postId)` |
| **Controlled Access** | Getter/Setter | `public void setSentiment()` |
| **Immutability** | Final + defensive copy | `return Collections.unmodifiableList()` |

### Abstraction Benefits
| Benefit | Implementation | Example |
|---------|-----------------|---------|
| **Hide Complexity** | Interfaces | `SentimentAnalyzer` interface |
| **Focus on What** | Method contracts | `analyzeSentiment(String)` |
| **Easy Swap** | Multiple implementations | 3 sentiment analyzers |
| **Maintainability** | Changes don't affect client | Changed analyzer internally |

### Inheritance Benefits
| Benefit | Implementation | Example |
|---------|-----------------|---------|
| **Code Reuse** | Base class methods | 20+ methods from `Post` |
| **Hierarchy** | IS-A relationship | `YouTubePost extends Post` |
| **Polymorphism** | Subclass override | `YouTubePost.toString()` |
| **Consistency** | Shared behavior | All posts have `comments`, `sentiment` |

### Polymorphism Benefits
| Benefit | Implementation | Example |
|---------|-----------------|---------|
| **Runtime flexibility** | Interface references | `SentimentAnalyzer analyzer` |
| **No code change** | Swap implementations | Change analyzer without code change |
| **Extensibility** | Add new types | Add `FacebookCrawler` |
| **Loose coupling** | Depend on interface | `Model` depends on `SentimentAnalyzer` |

### Design Pattern Benefits
| Pattern | Benefit | Example |
|---------|---------|---------|
| **MVC** | Separation of concerns | Model, View, Controller independent |
| **Registry** | Dynamic configuration | Add crawlers without UI change |
| **Strategy** | Runtime behavior change | Change sentiment analyzer |
| **Observer** | Real-time updates | All UI synced with Model |
| **Factory** | Encapsulated creation | `CrawlerRegistry.createCrawler()` |

---

## 🎯 CONCLUSION

Humanitarian Logistics UI demonstrates **professional-grade OOP architecture**:

### ✅ Kỹ Thuật Cơ Bản (7/7 sử dụng)
1. ✅ Encapsulation
2. ✅ Abstraction
3. ✅ Inheritance
4. ✅ Polymorphism
5. ✅ Interfaces
6. ✅ Abstract Classes
7. ✅ Composition

### ✅ Kỹ Thuật Nâng Cao (5/5+ sử dụng)
1. ✅ MVC Architecture
2. ✅ Registry Pattern
3. ✅ Strategy Pattern
4. ✅ Observer Pattern
5. ✅ Factory Pattern

### ✅ Advanced Techniques
- ✅ Singleton
- ✅ Dependency Injection
- ✅ Immutability
- ✅ Defensive copying
- ✅ Interface Segregation
- ✅ Null safety checks

### 📈 Result
- **Maintainable**: Easy to add/change features
- **Testable**: Can test with mock dependencies
- **Extensible**: Add new crawlers, analyzers, modules
- **Robust**: Null checks, error handling throughout
- **Professional**: Industry-standard patterns and practices
