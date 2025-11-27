package com.humanitarian.devui.crawler;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.sentiment.EnhancedSentimentAnalyzer;
import com.humanitarian.devui.sentiment.SentimentAnalyzer;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Facebook Crawler - Phân biệt rõ Hashtag và Keyword
 * - Hashtag: https://www.facebook.com/hashtag/yagi?locale=vi_VN
 * - Keyword: https://www.facebook.com/search/posts?q=yagi&locale=vi_VN
 */
public class FacebookCrawler implements DataCrawler {
    private WebDriver driver;
    private WebDriverWait wait;
    private boolean initialized;
    private SentimentAnalyzer sentimentAnalyzer;
    private static final int SCROLL_PAUSE = 2000;
    private String cookieString = "";
    private String email = "bedepzaibodoi@icloud.com";
    private String password = "Bemane1234@";

    public FacebookCrawler() {
        this.initialized = false;
        this.sentimentAnalyzer = new EnhancedSentimentAnalyzer();
    }

    public FacebookCrawler(String cookieString) {
        this.initialized = false;
        this.cookieString = cookieString;
        this.sentimentAnalyzer = new EnhancedSentimentAnalyzer();
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    public void initialize() {
        try {
            System.out.println("🚀 Initializing Facebook Crawler with Direct Login...");

            // Initialize sentiment analyzer
            sentimentAnalyzer.initialize();

            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--window-size=1920,1080",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-blink-features=AutomationControlled",
                    "--disable-gpu",
                    "--start-maximized",
                    "disable-infobars",
                    "--disable-extensions"
            );
            options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);

            System.out.println("✓ Creating ChromeDriver...");
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            System.out.println("✓ Logging in with direct credentials...");
            loginWithCredentials();

            initialized = true;
            System.out.println("✅ Facebook Crawler initialized successfully");

        } catch (Exception e) {
            System.err.println("❌ Error initializing: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
            if (driver != null) driver.quit();
        }
    }

    public void loadCookieFromFile(String filePath) {
        try {
            System.out.println("📂 Loading cookies from: " + filePath);
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            
            if (!java.nio.file.Files.exists(path)) {
                System.err.println("❌ ERROR: File not found: " + filePath);
                System.err.println("Please ensure cookie.txt exists in the project root directory");
                return;
            }
            
            this.cookieString = java.nio.file.Files.readString(path).trim();
            
            if (cookieString.isEmpty()) {
                System.err.println("❌ ERROR: cookie.txt is empty");
                return;
            }
            
            // Count cookies
            int cookieCount = cookieString.split(";").length;
            System.out.println("✓ Loaded " + cookieCount + " cookies from file");
            System.out.println("✓ Total cookie string length: " + cookieString.length() + " characters");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to load cookie file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Login with direct credentials (email + password)
     * More reliable than cookie-based login
     */
    private void loginWithCredentials() {
        try {
            System.out.println("\n📋 Step 1: Navigate to Facebook login page");
            driver.get("https://www.facebook.com/login");
            Thread.sleep(3000);
            System.out.println("✓ Login page loaded");

            System.out.println("\n📋 Step 2: Enter email");
            WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
            emailField.clear();
            emailField.sendKeys(email);
            System.out.println("✓ Email entered: " + email);

            System.out.println("\n📋 Step 3: Enter password");
            WebElement passwordField = driver.findElement(By.id("pass"));
            passwordField.clear();
            passwordField.sendKeys(password);
            System.out.println("✓ Password entered");

            System.out.println("\n📋 Step 4: Click login button");
            WebElement loginButton = driver.findElement(By.name("login"));
            loginButton.click();
            System.out.println("✓ Login button clicked");

            System.out.println("\n📋 Step 5: Wait for login to complete (20 seconds)");
            Thread.sleep(20000);  // Long wait for login to complete
            
            String currentUrl = driver.getCurrentUrl();
            System.out.println("  Current URL: " + currentUrl);

            System.out.println("\n📋 Step 6: Verify login status");
            if (currentUrl.contains("login") || currentUrl.contains("checkpoint")) {
                System.err.println("❌ Still on login page - Login failed!");
                System.err.println("  Check if credentials are correct or if 2FA is required");
                return;
            }

            // Additional check: look for user menu
            try {
                driver.findElement(By.xpath("//a[@aria-label='Your profile'] | //button[contains(@aria-label, 'Account')]"));
                System.out.println("✓ Profile menu found - Login successful!");
            } catch (Exception e) {
                System.out.println("⚠ Could not find profile menu, but not on login page");
            }

            System.out.println("\n✅ Direct login completed successfully!");
            System.out.println("Ready to crawl Facebook posts!\n");

        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setCookie(String cookieString) {
        this.cookieString = cookieString;
    }

    /**
     * Crawl bài viết từ cả hashtag và keyword
     * Ưu tiên hashtag trước (chính xác hơn), sau đó mới tìm keyword nếu cần thêm dữ liệu
     */
    @Override
    public List<Post> crawlPosts(List<String> keywords, List<String> hashtags, int limit) {
        List<Post> posts = new ArrayList<>();
        if (!initialized) initialize();
        if (!initialized || driver == null) return posts;

        int remaining = limit;

        // === Gộp tất cả từ khóa vào một danh sách và phân loại tự động ===
        List<String> allTerms = new ArrayList<>();
        if (keywords != null) allTerms.addAll(keywords);
        if (hashtags != null) allTerms.addAll(hashtags);

        // Danh sách để tránh trùng lặp (dùng lowercase để so sánh)
        Set<String> processedTerms = new HashSet<>();

        for (String term : allTerms) {
            if (posts.size() >= limit) break;

            String originalTerm = term.trim();
            if (originalTerm.isEmpty()) continue;

            String cleanTerm = originalTerm.replaceFirst("^#+", ""); // bỏ tất cả dấu # ở đầu
            if (cleanTerm.isEmpty()) continue;

            String lowerTerm = cleanTerm.toLowerCase();
            if (processedTerms.contains(lowerTerm)) continue; // tránh crawl trùng
            processedTerms.add(lowerTerm);

            String url;
            boolean isHashtag = originalTerm.startsWith("#");

            if (isHashtag) {
                // → Đây là hashtag → dùng link hashtag
                url = "https://www.facebook.com/hashtag/" + cleanTerm + "?locale=vi_VN";
                System.out.println("Crawling HASHTAG: #" + cleanTerm + " → " + url);
            } else {
                // → Đây là keyword → dùng link tìm kiếm
                String encoded = cleanTerm.replace(" ", "%20");
                url = "https://www.facebook.com/search/posts?q=" + encoded + "&locale=vi_VN";
                System.out.println("Crawling KEYWORD: \"" + cleanTerm + "\" → " + url);
            }

            // Crawl từ URL này, lấy tối đa 30 bài mỗi từ khóa/hashtag để tránh bias
            int take = Math.min(remaining, 30);
            crawlFromUrl(url, take, posts);
            remaining = limit - posts.size();
        }

        System.out.println("Total posts crawled: " + posts.size());
        return posts;
    }

    /**
     * Crawl a single Facebook post by URL and extract all comments
     * Skips login and crawls directly without authentication
     * @param postUrl The URL of the Facebook post
     * @return FacebookPost object with all extracted comments, or null if failed
     */
    public FacebookPost crawlPostByUrl(String postUrl) {
        FacebookCrawler tempCrawler = null;
        try {
            System.out.println("\n🔗 Crawling single post from URL (no login required)");
            System.out.println("📍 URL: " + postUrl);
            
            // Validate URL
            if (!postUrl.contains("facebook.com")) {
                System.err.println("❌ Invalid URL: Must be a Facebook URL");
                return null;
            }

            // Create a temporary crawler that skips login
            System.out.println("🚀 Initializing browser (no login)...");
            tempCrawler = new FacebookCrawler();
            tempCrawler.initializeBrowserOnly();  // Skip login

            if (!tempCrawler.isInitialized()) {
                System.err.println("❌ Failed to initialize browser");
                return null;
            }

            System.out.println("✓ Browser ready");

            // Navigate to the post directly (no login needed for public posts)
            System.out.println("📍 Navigating to post...");
            tempCrawler.driver.get(postUrl);
            Thread.sleep(3000);

            String currentUrl = tempCrawler.driver.getCurrentUrl();
            System.out.println("  Current URL: " + currentUrl);

            // Wait for content to load
            tempCrawler.wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            System.out.println("✓ Post page loaded");

            // Extract post content
            String postId = "FB_POST_" + postUrl.hashCode();
            String postContent = tempCrawler.extractPostContentDirect();
            
            System.out.println("📝 Post content extracted");

            // Create post object
            FacebookPost post = new FacebookPost(
                postId,
                postContent,
                LocalDateTime.now().minusHours(new Random().nextInt(72)),
                "Facebook User",
                "FACEBOOK"
            );

            // Analyze sentiment instead of using default
            Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(postContent);
            post.setSentiment(sentiment);
            ReliefItem reliefItem = tempCrawler.classifyReliefItemFromText(postContent);
            post.setReliefItem(reliefItem);
            
            // Auto-detect and set disaster type from content
            DisasterType detectedDisaster = DisasterManager.getInstance().findDisasterTypeForPost(postContent);
            if (detectedDisaster != null) {
                post.setDisasterType(detectedDisaster);
            }

            // Extract comments
            System.out.println("💬 Extracting comments from post...");
            tempCrawler.extractCommentsFromDetailPageDirect(post);

            System.out.println("✅ Successfully extracted post with " + post.getComments().size() + " comments");
            return post;

        } catch (Exception e) {
            System.err.println("❌ Error crawling post by URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // Clean up temporary crawler
            if (tempCrawler != null) {
                try {
                    tempCrawler.shutdown();
                } catch (Exception e) {
                    System.err.println("Error shutting down temporary crawler: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Initialize browser only (skip login) for direct URL crawling
     */
    public void initializeBrowserOnly() {
        try {
            System.out.println("🚀 Initializing browser without login...");

            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--window-size=1920,1080",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-blink-features=AutomationControlled",
                    "--disable-gpu",
                    "--start-maximized",
                    "disable-infobars",
                    "--disable-extensions"
            );
            options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);

            System.out.println("✓ Creating ChromeDriver...");
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            initialized = true;
            System.out.println("✅ Browser initialized successfully (no login needed)");

        } catch (Exception e) {
            System.err.println("❌ Error initializing browser: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
            if (driver != null) driver.quit();
        }
    }

    /**
     * Extract post content (accessible without login)
     */
    public String extractPostContentDirect() {
        try {
            // Try to find post message/content
            List<WebElement> contentElements = driver.findElements(By.xpath(
                "//div[@data-testid='post_message'] | //div[@data-testid='post'] | //span[@dir='auto']"
            ));
            
            if (!contentElements.isEmpty()) {
                String content = contentElements.get(0).getText();
                if (content != null && !content.isEmpty()) {
                    return content;
                }
            }
            
            // Fallback: Get text from main article
            List<WebElement> articles = driver.findElements(By.xpath(
                "//article | //div[contains(@role, 'article')]"
            ));
            
            if (!articles.isEmpty()) {
                String content = articles.get(0).getText();
                if (content != null && !content.isEmpty()) {
                    return content.substring(0, Math.min(500, content.length()));
                }
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ Could not extract post content: " + e.getMessage());
        }
        
        return "Post from Facebook";
    }

    /**
     * Extract comments from post detail page (accessible without login)
     */
    public void extractCommentsFromDetailPageDirect(FacebookPost post) {
        try {
            System.out.println("    📝 Extracting comments from post detail page...");
            System.out.println("      Current URL: " + driver.getCurrentUrl());

            // Scroll xuống để load comments
            JavascriptExecutor js = (JavascriptExecutor) driver;
            int scrollCount = 0;
            int maxScrolls = 8;

            System.out.println("      🔄 Scrolling down to load comments...");
            while (scrollCount < maxScrolls) {
                js.executeScript("window.scrollBy(0, 600);");
                Thread.sleep(1200);
                scrollCount++;
            }
            System.out.println("      ✓ Scrolled " + scrollCount + " times");

            // Tìm comments
            System.out.println("      🔍 Looking for comment elements...");
            
            List<WebElement> commentElements = new ArrayList<>();
            
            // Strategy 1: Tìm comment elements trực tiếp
            try {
                List<WebElement> strategy1 = driver.findElements(By.xpath(
                    "//div[@data-testid='comment']"
                ));
                if (!strategy1.isEmpty()) {
                    System.out.println("      ✓ Found " + strategy1.size() + " comments (data-testid='comment')");
                    commentElements.addAll(strategy1);
                }
            } catch (Exception e) {
                System.out.println("      ⚠️ Strategy 1 (data-testid='comment'): " + e.getMessage());
            }

            // Strategy 2: Tìm các article elements chứa comments
            if (commentElements.isEmpty()) {
                try {
                    List<WebElement> strategy2 = driver.findElements(By.xpath(
                        "//div[contains(@class, 'comment')]"
                    ));
                    if (!strategy2.isEmpty()) {
                        System.out.println("      ✓ Found " + strategy2.size() + " comments (class='comment')");
                        commentElements.addAll(strategy2);
                    }
                } catch (Exception e) {
                    System.out.println("      ⚠️ Strategy 2 (class='comment'): " + e.getMessage());
                }
            }

            // Strategy 3: Tìm divs chứa text spans
            if (commentElements.isEmpty()) {
                try {
                    List<WebElement> strategy3 = driver.findElements(By.xpath(
                        "//div[contains(@role, 'article') and .//span[@dir='auto']]"
                    ));
                    if (!strategy3.isEmpty()) {
                        System.out.println("      ✓ Found " + strategy3.size() + " elements with role='article'");
                        commentElements.addAll(strategy3);
                    }
                } catch (Exception e) {
                    System.out.println("      ⚠️ Strategy 3 (role='article'): " + e.getMessage());
                }
            }

            if (commentElements.isEmpty()) {
                System.out.println("      ⚠️ No comment elements found");
                return;
            }

            // Extract comments
            int commentCount = 0;
            for (WebElement commentElement : commentElements) {
                if (commentCount >= 20) break;

                try {
                    // Lấy text của comment
                    String commentText = null;
                    List<WebElement> textSpans = commentElement.findElements(By.xpath(
                        ".//span[@dir='auto']"
                    ));
                    
                    if (!textSpans.isEmpty()) {
                        commentText = textSpans.get(0).getText();
                    } else {
                        commentText = commentElement.getText();
                    }

                    if (commentText == null || commentText.trim().isEmpty() || commentText.length() < 3) {
                        continue;
                    }

                    // Lấy tên người bình luận
                    String commenterName = "Anonymous";
                    try {
                        List<WebElement> nameElements = commentElement.findElements(By.xpath(
                            ".//strong[1]"
                        ));
                        
                        if (!nameElements.isEmpty() && !nameElements.get(0).getText().trim().isEmpty()) {
                            commenterName = nameElements.get(0).getText().trim();
                        } else {
                            List<WebElement> linkNames = commentElement.findElements(By.xpath(
                                ".//a[@role='link'][1]"
                            ));
                            if (!linkNames.isEmpty()) {
                                String linkText = linkNames.get(0).getText().trim();
                                if (!linkText.isEmpty() && linkText.length() < 100) {
                                    commenterName = linkText;
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        System.out.println("      ⚠️ Could not extract commenter name");
                    }

                    // Tạo Comment object
                    Comment comment = new Comment(
                            "CMT_" + post.getPostId() + "_" + commentCount,
                            post.getPostId(),
                            commentText.trim(),
                            LocalDateTime.now(),
                            commenterName
                    );

                    post.addComment(comment);
                    commentCount++;

                    System.out.println("      ✓ Comment #" + commentCount + " from: " + 
                        commenterName.substring(0, Math.min(25, commenterName.length())));

                } catch (Exception e) {
                    System.out.println("      ⚠️ Error processing comment: " + e.getMessage());
                    continue;
                }
            }

            System.out.println("      ✅ Extracted " + commentCount + " comments total");

        } catch (Exception e) {
            System.err.println("      ❌ Error extracting comments: " + e.getMessage());
        }
    }

    private void crawlFromUrl(String url, int limit, List<Post> posts) {
        try {
            System.out.println("🌐 Navigating to: " + url);
            driver.get(url);
            Thread.sleep(4000);

            // Kiểm tra xem có bị redirect về login không
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("login") || currentUrl.contains("checkpoint")) {
                System.err.println("❌ ERROR: Redirected to login page!");
                System.err.println("Cookie login may have expired or is invalid");
                throw new Exception("Cookie authentication failed - redirected to login");
            }

            // Đợi nội dung load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            System.out.println("✓ Page loaded successfully");

            // Track đã crawl URLs để tránh trùng lặp
            Set<String> collectedPostUrls = new HashSet<>();
            
            int scrolls = 0;
            int maxScrolls = (limit / 4) + 10;

            System.out.println("\n📍 Phase 1: Scroll feed to collect post links...");
            while (collectedPostUrls.size() < limit && scrolls < maxScrolls) {
                scrollDown();
                Thread.sleep(SCROLL_PAUSE + new Random().nextInt(1000));
                
                // Collect URLs by clicking share button and copying links
                collectPostUrlsFromFeedViaShareButton(collectedPostUrls, limit);
                scrolls++;
            }

            System.out.println("\n📍 Phase 2: Visit each collected URL to extract comments...");
            System.out.println("📋 Total URLs collected: " + collectedPostUrls.size());
            
            // Now visit each collected URL to extract posts and comments
            int processedCount = 0;
            for (String postUrl : collectedPostUrls) {
                if (posts.size() >= limit) break;
                
                try {
                    System.out.println("\n  [" + (processedCount + 1) + "/" + collectedPostUrls.size() + "] " +
                        "Processing post: " + postUrl.substring(0, Math.min(80, postUrl.length())));
                    
                    // Navigate to post
                    driver.get(postUrl);
                    Thread.sleep(2000);
                    
                    // Create and populate post object
                    FacebookPost post = extractPostContent(postUrl);
                    
                    // Extract comments
                    System.out.println("    📝 Extracting comments...");
                    extractCommentsFromDetailPage(post);
                    
                    posts.add(post);
                    processedCount++;
                    
                    System.out.println("    ✅ Post with " + post.getComments().size() + " comments added");
                    
                } catch (Exception e) {
                    System.out.println("    ⚠️ Error processing post: " + e.getMessage());
                }
            }

            System.out.println("\n✅ Completed crawl: " + posts.size() + " posts with comments extracted");

        } catch (Exception e) {
            System.err.println("❌ Error crawling URL " + url + ": " + e.getMessage());
        }
    }

    private void scrollDown() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0, 800);");
    }

    /**
     * Collect post URLs from current feed page by clicking share buttons
     * and copying the post link to clipboard
     */
    private void collectPostUrlsFromFeedViaShareButton(Set<String> collectedUrls, int limit) {
        try {
            // Find all post containers on current page
            List<WebElement> postContainers = driver.findElements(By.xpath(
                "//div[contains(@data-testid, 'post') or contains(@role, 'article')]"
            ));
            
            System.out.println("    📋 Found " + postContainers.size() + " post containers on page");
            
            if (postContainers.isEmpty()) {
                return;
            }
            
            // Process each post container
            for (WebElement postContainer : postContainers) {
                if (collectedUrls.size() >= limit) break;
                
                try {
                    // Find the share button in this post
                    // Share button is usually an icon/button with aria-label or title containing 'share'
                    WebElement shareButton = findShareButton(postContainer);
                    
                    if (shareButton == null) {
                        System.out.println("      ⚠️ Share button not found for this post");
                        continue;
                    }
                    
                    System.out.println("      🔗 Found share button, clicking...");
                    
                    // Scroll element into view
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("arguments[0].scrollIntoView(true);", shareButton);
                    Thread.sleep(500);
                    
                    // Click share button
                    shareButton.click();
                    Thread.sleep(800);  // Wait for share menu to appear
                    
                    // Look for "Copy link" option in share menu
                    WebElement copyLinkOption = findCopyLinkOption();
                    
                    if (copyLinkOption == null) {
                        System.out.println("      ⚠️ 'Copy link' option not found in share menu");
                        // Close menu by pressing Escape
                        try {
                            driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
                            Thread.sleep(300);
                        } catch (Exception ignored) {}
                        continue;
                    }
                    
                    System.out.println("      📋 Clicking 'Copy link' option...");
                    copyLinkOption.click();
                    Thread.sleep(500);
                    
                    // Extract link from clipboard
                    String copiedLink = getClipboardContent();
                    
                    if (copiedLink != null && !copiedLink.isEmpty() && copiedLink.contains("facebook.com")) {
                        // Normalize the URL
                        copiedLink = normalizePostUrl(copiedLink);
                        
                        if (!collectedUrls.contains(copiedLink)) {
                            collectedUrls.add(copiedLink);
                            System.out.println("      ✓ Link collected: " + 
                                copiedLink.substring(0, Math.min(60, copiedLink.length())));
                        }
                    } else {
                        System.out.println("      ⚠️ Failed to get valid link from clipboard");
                    }
                    
                } catch (Exception e) {
                    System.out.println("      ⚠️ Error processing post: " + e.getMessage());
                    // Try to close any open menus
                    try {
                        driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
                        Thread.sleep(300);
                    } catch (Exception ignored) {}
                }
            }
            
        } catch (Exception e) {
            System.out.println("    ⚠️ Error collecting post URLs: " + e.getMessage());
        }
    }

    /**
     * Find the share button in a post container
     * Share button can be in different formats depending on Facebook UI
     */
    private WebElement findShareButton(WebElement postContainer) {
        try {
            // Strategy 1: Find button with data-testid='share' or similar
            try {
                return postContainer.findElement(By.xpath(
                    ".//button[contains(@aria-label, 'Share') or contains(@title, 'Share')]"
                ));
            } catch (Exception e1) {
                // Strategy 2: Find icon button for sharing
                try {
                    return postContainer.findElement(By.xpath(
                        ".//div[@role='button'][contains(@aria-label, 'Share') or @data-testid='share']"
                    ));
                } catch (Exception e2) {
                    // Strategy 3: Find share option in post action menu
                    try {
                        // Look for menu button first
                        WebElement menuButton = postContainer.findElement(By.xpath(
                            ".//button[contains(@aria-label, 'More')]"
                        ));
                        return menuButton;  // Return menu button, we'll handle it differently
                    } catch (Exception e3) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Find the "Copy link" option in the share menu
     */
    private WebElement findCopyLinkOption() {
        try {
            // Look for "Copy link" option in popup menu
            List<WebElement> options = driver.findElements(By.xpath(
                "//*[contains(text(), 'Copy link') or contains(text(), 'copy link')]"
            ));
            
            if (!options.isEmpty()) {
                return options.get(0);
            }
            
            // Alternative: Look for any menu item that might contain link copying
            List<WebElement> menuItems = driver.findElements(By.xpath(
                "//div[@role='menuitem'] | //button[@role='menuitem'] | //a[@role='menuitem']"
            ));
            
            for (WebElement item : menuItems) {
                String text = item.getText().toLowerCase();
                if (text.contains("copy") && text.contains("link")) {
                    return item;
                }
            }
            
        } catch (Exception e) {
            System.out.println("        ⚠️ Error finding copy link option: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get clipboard content using system command (macOS/Linux/Windows compatible)
     */
    @SuppressWarnings("deprecation")
    private String getClipboardContent() {
        try {
            // For macOS
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                Process process = Runtime.getRuntime().exec("pbpaste");
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
                );
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                return content.toString().trim();
            }
            // For Windows
            else if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Process process = Runtime.getRuntime().exec("powershell -Command \"Get-Clipboard\"");
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
                );
                return reader.readLine();
            }
            // For Linux
            else {
                Process process = Runtime.getRuntime().exec("xclip -selection clipboard -o");
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream())
                );
                return reader.readLine();
            }
        } catch (Exception e) {
            System.out.println("        ⚠️ Could not read clipboard: " + e.getMessage());
            return null;
        }
    }

    /**
     * Normalize post URL to ensure consistency
     */
    private String normalizePostUrl(String url) {
        // Remove query parameters and fragments
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.contains("#")) {
            url = url.substring(0, url.indexOf("#"));
        }
        return url;
    }

    /**
     * Extract post content from post detail page
     */
    private FacebookPost extractPostContent(String postUrl) {
        try {
            // Extract post ID from URL
            String postId = "FB_POST_" + postUrl.hashCode();
            
            // Get post text/content
            String postContent = "Post content";
            try {
                List<WebElement> contentElements = driver.findElements(By.xpath(
                    "//div[@data-testid='post_message'] | //span[@dir='auto']"
                ));
                if (!contentElements.isEmpty()) {
                    postContent = contentElements.get(0).getText();
                }
            } catch (Exception ignored) {}
            
            // Create post object
            FacebookPost post = new FacebookPost(
                postId,
                postContent,
                LocalDateTime.now().minusHours(new Random().nextInt(72)),
                "Facebook User",
                "FACEBOOK"
            );
            
            // Analyze sentiment instead of using default
            Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(postContent);
            post.setSentiment(sentiment);
            
            // Try to classify relief item from content
            ReliefItem reliefItem = classifyReliefItemFromText(postContent);
            post.setReliefItem(reliefItem);
            
            // Auto-detect and set disaster type from content
            DisasterType detectedDisaster = DisasterManager.getInstance().findDisasterTypeForPost(postContent);
            if (detectedDisaster != null) {
                post.setDisasterType(detectedDisaster);
            }
            
            return post;
            
        } catch (Exception e) {
            System.out.println("      ⚠️ Error extracting post content: " + e.getMessage());
            // Return default post
            FacebookPost defaultPost = new FacebookPost(
                "FB_POST_" + postUrl.hashCode(),
                "Post from Facebook",
                LocalDateTime.now(),
                "Facebook User",
                "FACEBOOK"
            );
            return defaultPost;
        }
    }

    /**
     * Classify relief item from text content
     */
    private ReliefItem classifyReliefItemFromText(String text) {
        String t = text.toLowerCase();
        if (t.matches(".*(tiền|hỗ trợ|cấp|trợ cấp|quyên góp).*")) {
            return new ReliefItem(ReliefItem.Category.CASH, "Hỗ trợ tiền", 5);
        }
        if (t.matches(".*(y tế|thuốc|bệnh|viện|bác sĩ|sức khỏe|chữa trị).*")) {
            return new ReliefItem(ReliefItem.Category.MEDICAL, "Hỗ trợ y tế", 5);
        }
        if (t.matches(".*(nhà|mái|ở|nơi trú|tạm trú|lều|chỗ ở).*")) {
            return new ReliefItem(ReliefItem.Category.SHELTER, "Nơi ở tạm", 5);
        }
        if (t.matches(".*(ăn|cơm|thực phẩm|lương thực|nước|đói|khát).*")) {
            return new ReliefItem(ReliefItem.Category.FOOD, "Thực phẩm & nước", 5);
        }
        if (t.matches(".*(xe|vận chuyển|đi lại|di tản|cứu hộ).*")) {
            return new ReliefItem(ReliefItem.Category.TRANSPORTATION, "Vận chuyển", 4);
        }
        return new ReliefItem(ReliefItem.Category.CASH, "Hỗ trợ chung", 2);
    }

    // === Các phương thức hỗ trợ khác giữ nguyên (crawlComments, extractText, analyzeSentiment, v.v.) ===
    // (Giữ nguyên như code gốc của bạn để tránh dài dòng, chỉ thay đổi nhỏ nếu cần)

    /**
     * Crawl comments từ trang chi tiết bài viết
     * Kéo xuống để load comments, sau đó extract
     */
    private void extractCommentsFromDetailPage(FacebookPost post) {
        try {
            System.out.println("    📝 Extracting comments from post detail page...");
            
            // First: Scroll down on current page to load comments
            String postUrl = driver.getCurrentUrl();
            System.out.println("      Current URL: " + postUrl);
            
            JavascriptExecutor js = (JavascriptExecutor) driver;
            int scrollCount = 0;
            int maxScrolls = 5; // Scroll to load comments
            
            System.out.println("      🔄 Scrolling down to load comments...");
            while (scrollCount < maxScrolls) {
                js.executeScript("window.scrollBy(0, 600);");
                Thread.sleep(1500);  // Wait for comments to load
                scrollCount++;
            }
            System.out.println("      ✓ Scrolled " + scrollCount + " times");
            
            // Second: Extract post ID and navigate to mbasic version
            String postId = extractPostIdFromUrl(postUrl);
            
            if (postId == null || postId.isEmpty()) {
                System.out.println("      ⚠️ Could not extract post ID from URL: " + postUrl);
                return;
            }
            
            // Navigate to mbasic version for better comment parsing
            String mbasicUrl = "https://mbasic.facebook.com/" + postId;
            System.out.println("      🔗 Navigating to mbasic: " + mbasicUrl);
            driver.get(mbasicUrl);
            Thread.sleep(3000);
            
            // Scroll down on mbasic page to load comments there too
            System.out.println("      🔄 Scrolling on mbasic page...");
            for (int i = 0; i < 3; i++) {
                js.executeScript("window.scrollBy(0, 600);");
                Thread.sleep(1200);
            }
            
            // Extract comments using mbasic structure
            int commentCount = 0;
            boolean hasMoreComments = true;
            
            while (hasMoreComments && commentCount < 20) {
                try {
                    // Find comment reply links
                    List<WebElement> commentLinks = driver.findElements(By.xpath(
                        "//a[contains(@href, \"comment/replies\")]"
                    ));
                    
                    System.out.println("      ✓ Found " + commentLinks.size() + " comment reply links");
                    
                    for (WebElement commentLink : commentLinks) {
                        if (commentCount >= 20) break;
                        
                        try {
                            // Get comment ID from ctoken parameter
                            String href = commentLink.getAttribute("href");
                            String ctoken = extractCtoken(href);
                            
                            if (ctoken == null || ctoken.isEmpty()) continue;
                            
                            // Extract comment element by ID
                            String commentId = ctoken.split("_")[1];
                            String xpathId = "//*[@id=\"" + commentId + "\"]";
                            
                            List<WebElement> commentElements = driver.findElements(By.xpath(xpathId));
                            
                            for (WebElement commentElement : commentElements) {
                                try {
                                    String elementText = commentElement.getText().trim();
                                    if (elementText.isEmpty() || elementText.length() < 3) continue;
                                    
                                    // Parse comment: author, content, timestamp
                                    String[] parts = parseCommentText(elementText, post.getCreatedAt());
                                    String author = parts[0];
                                    String content = parts[1];
                                    LocalDateTime timestamp = LocalDateTime.parse(parts[2]);
                                    
                                    if (content == null || content.length() < 3) continue;
                                    
                                    // Create comment
                                    Comment comment = new Comment(
                                        "CMT_" + post.getPostId() + "_" + commentCount,
                                        post.getPostId(),
                                        content,
                                        timestamp,
                                        author
                                    );
                                    
                                    post.addComment(comment);
                                    commentCount++;
                                    
                                    System.out.println("      ✓ Comment #" + commentCount + " from: " + 
                                        author.substring(0, Math.min(25, author.length())));
                                    
                                } catch (Exception e) {
                                    System.out.println("      ⚠️ Error processing comment element: " + e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("      ⚠️ Error extracting comment link: " + e.getMessage());
                        }
                    }
                    
                    // Look for "next" button to load more comments
                    try {
                        List<WebElement> nextBtns = driver.findElements(By.xpath(
                            "//*[contains(@id,\"see_next\")]/a"
                        ));
                        
                        if (!nextBtns.isEmpty()) {
                            System.out.println("      🔄 Found next button, loading more comments...");
                            nextBtns.get(0).click();
                            Thread.sleep(2000);
                        } else {
                            hasMoreComments = false;
                        }
                    } catch (Exception e) {
                        hasMoreComments = false;
                    }
                    
                } catch (Exception e) {
                    System.out.println("      ⚠️ Error in comment extraction loop: " + e.getMessage());
                    hasMoreComments = false;
                }
            }
            
            System.out.println("      ✅ Extracted " + commentCount + " comments");
            
        } catch (Exception e) {
            System.err.println("      ❌ Error extracting comments: " + e.getMessage());
        }
    }
    
    private String extractPostIdFromUrl(String url) {
        try {
            // Handle different Facebook URL formats
            // https://www.facebook.com/username/posts/123456
            // https://www.facebook.com/photo.php?fbid=123456
            // https://www.facebook.com/photo/?fbid=123456
            
            if (url.contains("fbid=")) {
                return url.split("fbid=")[1].split("[&?]")[0];
            } else if (url.contains("/posts/")) {
                return url.split("/posts/")[1].split("[?/]")[0];
            } else if (url.contains("/permalink.php?story_fbid=")) {
                return url.split("story_fbid=")[1].split("[&]")[0];
            }
        } catch (Exception e) {
            System.out.println("      ⚠️ Error extracting post ID: " + e.getMessage());
        }
        return null;
    }
    
    private String extractCtoken(String href) {
        try {
            if (href.contains("ctoken=")) {
                return href.split("ctoken=")[1].split("&")[0];
            }
        } catch (Exception e) {
            // Silent fail
        }
        return null;
    }
    
    private String[] parseCommentText(String elementText, LocalDateTime referenceTime) {
        // Format: Author name ... content ... timestamp (e.g., "2m")
        // Returns [author, content, timestamp]
        
        String author = "Anonymous";
        String content = elementText;
        LocalDateTime timestamp = LocalDateTime.now();
        
        try {
            String[] lines = elementText.split("\n");
            
            if (lines.length >= 2) {
                // First line usually has author and timestamp
                String firstLine = lines[0];
                
                // Extract author (before "·" or similar separators)
                if (firstLine.contains("·")) {
                    author = firstLine.split("·")[0].trim();
                } else if (firstLine.contains("ago")) {
                    author = firstLine.replaceAll("\\d+[smhd]\\s*ago", "").trim();
                } else {
                    author = firstLine.split("\\s{2,}")[0].trim();
                }
                
                // Extract timestamp from first line
                String timePattern = "\\d+[smhd]|\\d+\\s*(phút|giờ|ngày|tuần|năm|minute|hour|day|week|year)";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(timePattern);
                java.util.regex.Matcher matcher = pattern.matcher(firstLine);
                
                if (matcher.find()) {
                    String timeStr = matcher.group();
                    timestamp = parseRelativeTime(timeStr, referenceTime);
                }
                
                // Content is remaining lines (skip first line which has metadata)
                if (lines.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < lines.length; i++) {
                        if (!lines[i].trim().isEmpty()) {
                            sb.append(lines[i].trim()).append(" ");
                        }
                    }
                    content = sb.toString().trim();
                }
            }
        } catch (Exception e) {
            // Use defaults
        }
        
        return new String[]{author, content, timestamp.toString()};
    }


    @Override
    public String getCrawlerName() {
        return "FacebookCrawler (Hashtag + Keyword)";
    }

    /**
     * Generate fallback data when live Facebook crawling fails or is unavailable.
     * Data includes:
     * - Category-specific content for each relief type
     * - Realistic temporal distribution (35 days with time-based sentiment evolution)
     * - Comments showing sentiment evolution per category
     */
    public List<Post> generateFallbackData(int limit) {
        System.out.println("📊 Generating fallback data with realistic temporal evolution...");
        List<Post> posts = new ArrayList<>();

        // Category-specific content templates simulating real Facebook posts
        Map<ReliefItem.Category, String[]> categoryContents = new HashMap<>();

        categoryContents.put(ReliefItem.Category.CASH, new String[]{
            "💰 Cash program launched! Registration at 5 centers.", "Families waiting: System issues delaying payments 2 days.",
            "Queue crisis: 500 families waiting hours at centers.", "⚠️ Complex requirements confusing families. Unclear criteria.",
            "Budget limits: Only 500k per household approved.", "Database problems halting cash disbursement entirely.",
            "Good news: Disbursement smooth! Hundreds supported.", "500 families in week 1. Process improving steadily.",
            "8 new centers opened. Payment faster now.", "Audit complete: Officials implementing improvements.",
            "Families starting small businesses! Positive impact.", "Economic activity increasing in community.",
            "Cash now effective! Families meeting basic needs.", "Success: Family of 5 in safe house thanks to cash.",
            "Healthcare access improved: Families buying medicine.", "Education: Children in school with uniforms.",
            "2000+ families supported. Community morale high.", "Third round successful: 100% satisfaction reported."
        });
        
        categoryContents.put(ReliefItem.Category.MEDICAL, new String[]{
            "🚑 First cases confirmed. Healthcare alert.", "Hospital overwhelmed! Only 1 doctor per 10,000.",
            "Medicine shortage critical: No antibiotics.", "Patients dying from preventable illnesses.",
            "❌ Healthcare system collapsing.", "Vaccination delayed: Supply shortage.",
            "Mobile clinic: 150+ patients treated.", "Vaccine shipment arrived! Vaccinations ongoing.",
            "Health improving: Better medicine access.", "Patients treated smoothly. Good care.",
            "Staff shortages improving slowly.", "Doctor-patient ratio normalized now.",
            "🏥 Maternal health: 99% pregnant women in care.", "Disease eradication 80% complete.",
            "Healthcare workers celebrated by communities.", "Medical supply chain now reliable.",
            "Health metrics improving dramatically.", "Disease cases declining significantly."
        });
        
        categoryContents.put(ReliefItem.Category.SHELTER, new String[]{
            "🏚️ Thousands homeless! Sleeping under trees.", "Urgent: Winter approaching. Families in danger.",
            "Rain crisis: Families wet and cold.", "Makeshift camps: Diseases spreading fast.",
            "⚠️ 5000 families homeless: Tent shortage critical.", "Dangerous structures with no walls.",
            "Schools invaded by displaced families.", "Winter will be deadly: Families unprepared.",
            "🏠 5 new locations built! Progress good.", "Families getting safe shelter now.",
            "Construction progressing. Hope visible.", "Shelter adequate and protective.",
            "Reconstruction starting. Communities optimistic.", "500 shelters completed this month.",
            "🏘️ Repairs before rainy season done.", "Permanent housing halfway complete!",
            "2000 houses rebuilt successfully.", "Communities rebuilding. Structures strong.",
            "Schools reopening: Children in classes.", "Home ownership returning. Families stable."
        });
        
        categoryContents.put(ReliefItem.Category.FOOD, new String[]{
            "🚨 Food crisis: No food available!", "Hunger spreading: Children malnourished.",
            "Prices skyrocketing: Families cannot buy.", "Starvation threat: Eating tree bark.",
            "Shortage critical: 1 cup rice daily.", "Children crying: Mothers rationing food.",
            "Deaths reported: Malnutrition victims.", "🍚 Distribution successful! Full supplies.",
            "Abundance at distribution points.", "Quality good: Families satisfied.",
            "Rice and vegetables in supply.", "Distribution smooth: Morale improving.",
            "Shipments arriving regularly now.", "Variety improving: Fish, oil available.",
            "Farmers restarting: Crops growing.", "Markets reopening: Food available.",
            "🌾 Harvest approaching: Security improving!", "Production 70% of normal now.",
            "Food prices stabilizing fast.", "Community gardens productive!",
            "School meals: 2 per day for children.", "Nutrition improving: Children gaining weight."
        });
        
        categoryContents.put(ReliefItem.Category.TRANSPORTATION, new String[]{
            "🚨 Roads destroyed: Communities isolated!", "Evacuation impossible: Debris blocking.",
            "❌ Transport system collapsed!", "Families stranded: No escape possible.",
            "Medical emergencies: Pregnant women stuck.", "Supply trucks cannot reach villages.",
            "Fuel shortage: Vehicles not operating.", "Bridges destroyed: 5 communities cut off.",
            "Walking 30km to nearest clinic.", "🚗 Vehicle fleet assembled! Ready.",
            "Transportation smooth: Reaching destinations.", "Medical transport reliable now.",
            "Coordination excellent: Routes established.", "Community mobility improved greatly.",
            "Main road cleared: District center reached.", "Emergency routes established.",
            "Temporary bridges built: Crossing possible.", "Maintenance network operational.",
            "Transport functioning: Regular schedules.", "Trade resumed: Vehicles carrying goods.",
            "Buses operating: Public transit restarted.", "Journey to city: 3 hours now.",
            "Fleet expanded: Operators resuming service.", "Employment: Hiring drivers and mechanics."
        });
        
        LocalDateTime baseTime = LocalDateTime.now().minusDays(90);
        int postIndex = 0;
        Random rand = new Random();

        // Generate MANY posts (80-100) spread across 90 days for realistic chart
        for (ReliefItem.Category category : ReliefItem.Category.values()) {
            String[] contents = categoryContents.get(category);
            int postsPerCategory = limit / 5 + rand.nextInt(8); // 16-23 posts per category

            for (int i = 0; i < postsPerCategory && postIndex < limit; i++) {
                // Spread across full 90 days with varied hours for realistic distribution
                int dayOffset = rand.nextInt(90);
                int hour = 6 + rand.nextInt(16);
                int minute = rand.nextInt(60);
                
                LocalDateTime postTime = baseTime.plusDays(dayOffset)
                    .plusHours(hour)
                    .plusMinutes(minute);

                // Sentiment mapping based on temporal progression
                double dayProgress = dayOffset / 90.0;
                int contentIndex = Math.min((int) (dayProgress * contents.length), contents.length - 1);
                
                String content = contents[contentIndex];

                ReliefItem reliefItem = new ReliefItem(
                    category,
                    "Relief: " + category.getDisplayName(),
                    rand.nextInt(5) + 1
                );

                FacebookPost post = new FacebookPost(
                    "FB_FALLBACK_" + category.name() + "_" + postIndex,
                    content,
                    postTime,
                    "Relief_Org_" + (postIndex % 5),
                    "FACEBOOK"
                );

                // Analyze sentiment instead of using default
                Sentiment analyzedSentiment = sentimentAnalyzer.analyzeSentiment(content);
                post.setSentiment(analyzedSentiment);
                post.setReliefItem(reliefItem);
                post.setLikes(rand.nextInt(1500) + 30);
                post.setShares(rand.nextInt(400) + 5);
                
                // Set disaster type - randomly assign from available disasters
                List<String> disasterNames = DisasterManager.getInstance().getAllDisasterNames();
                if (!disasterNames.isEmpty()) {
                    String randomDisaster = disasterNames.get(rand.nextInt(disasterNames.size()));
                    DisasterType disasterType = DisasterManager.getInstance().findDisasterType(randomDisaster);
                    if (disasterType != null) {
                        post.setDisasterType(disasterType);
                    }
                }

                // Add comments with varied sentiment
                addFallbackComments(post, category, contentIndex);

                posts.add(post);
                postIndex++;
            }
        }

        System.out.println("✅ Generated " + posts.size() + " fallback posts with temporal evolution");
        return posts;
    }

    /**
     * Add realistic comments to fallback posts, with sentiment matching post period
     */
    private void addFallbackComments(FacebookPost post, ReliefItem.Category category, int periodIndex) {
        Map<ReliefItem.Category, String[]> commentsByCategory = new HashMap<>();

        commentsByCategory.put(ReliefItem.Category.CASH, new String[]{
            "When start payments?", "Process is confusing!", "Finally some help!", "Registration took forever.",
            "Money not enough!", "Need more assistance.", "Long queue today.", "Still waiting 2 weeks.",
            "Grateful for cash!", "This helps so much.", "Business idea started.", "Can pay rent now.",
            "Kids in school again!", "Food security improved.", "Better times coming.", "Thank relief workers!"
        });

        commentsByCategory.put(ReliefItem.Category.MEDICAL, new String[]{
            "Doctor was rude!", "No medicines available.", "Waiting 4 hours.", "My child still sick!",
            "Dying from diseases.", "Very professional care.", "Got treated quickly.", "Much better now!",
            "Healthcare improved.", "Trust doctors now.", "Safe to visit clinic.", "Staff caring and kind."
        });

        commentsByCategory.put(ReliefItem.Category.SHELTER, new String[]{
            "Still homeless!", "Tent leaks badly.", "Roof collapsing!", "Need shelter urgently.",
            "Cold nights dangerous.", "Grateful for tent.", "Setup was fast.", "Very strong structure.",
            "Feels safe now.", "Family comfortable.", "Workers were kind.", "House rebuild started."
        });

        commentsByCategory.put(ReliefItem.Category.FOOD, new String[]{
            "Children hungry still!", "Same rice every day.", "No vegetables!", "Portions too small.",
            "Food quality bad.", "Spoiled items delivered.", "Food tasty today!", "Vegetables fresh!",
            "Enough for family.", "Nutrition improved.", "Kids healthy now.", "Market prices down."
        });

        commentsByCategory.put(ReliefItem.Category.TRANSPORTATION, new String[]{
            "Cannot leave area!", "30km walk to hospital.", "Truck never comes.", "Stranded here forever.",
            "No fuel available.", "Vehicle breakdown today.", "Roads finally cleared!", "Vehicles arriving!",
            "Got to hospital fast.", "Visit family now possible.", "Business deliveries work.", "Markets connected."
        });

        String[] authors = {"Ali", "Fatima", "Mohammed", "Amira", "Hassan", "Layla", "Omar", "Hana", "Yusuf", "Noor"};
        Random rand = new Random();

        int commentCount = 2 + rand.nextInt(4); // 2-5 comments per post
        String[] categoryComments = commentsByCategory.get(category);

        for (int i = 0; i < commentCount; i++) {
            String commentText;
            Sentiment.SentimentType sentiment;
            double confidence;

            // Better sentiment distribution based on period
            if (periodIndex < 5) {
                // Early: mostly negative/neutral
                double r = rand.nextDouble();
                if (r < 0.5) {
                    sentiment = Sentiment.SentimentType.NEGATIVE;
                    confidence = 0.75 + rand.nextDouble() * 0.20;
                } else {
                    sentiment = Sentiment.SentimentType.NEUTRAL;
                    confidence = 0.65 + rand.nextDouble() * 0.20;
                }
                commentText = categoryComments[rand.nextInt(Math.min(6, categoryComments.length))];
            } else if (periodIndex < 10) {
                // Mid: mixed
                double r = rand.nextDouble();
                if (r < 0.3) {
                    sentiment = Sentiment.SentimentType.NEGATIVE;
                } else if (r < 0.6) {
                    sentiment = Sentiment.SentimentType.NEUTRAL;
                } else {
                    sentiment = Sentiment.SentimentType.POSITIVE;
                }
                confidence = 0.65 + rand.nextDouble() * 0.25;
                commentText = categoryComments[rand.nextInt(categoryComments.length)];
            } else {
                // Late: mostly positive
                sentiment = rand.nextDouble() > 0.25 ? Sentiment.SentimentType.POSITIVE : Sentiment.SentimentType.NEUTRAL;
                confidence = 0.80 + rand.nextDouble() * 0.15;
                commentText = categoryComments[rand.nextInt(Math.min(7, categoryComments.length))];
            }

            Comment comment = new Comment(
                "CMT_FALLBACK_" + post.getPostId() + "_" + i,
                post.getPostId(),
                commentText,
                post.getCreatedAt().plusHours(1 + rand.nextInt(48)),
                authors[rand.nextInt(authors.length)]
            );

            comment.setSentiment(new Sentiment(sentiment, confidence, commentText));
            post.addComment(comment);
        }
    }

    @Override
    public void shutdown() {
        if (driver != null) {
            try {
                driver.quit();
                System.out.println("FacebookCrawler đã tắt");
            } catch (Exception e) {
                System.err.println("Lỗi khi tắt driver: " + e.getMessage());
            } finally {
                driver = null;
                initialized = false;
            }
        }
    }

    private LocalDateTime parseRelativeTime(String timeText, LocalDateTime referenceTime) {
        if (timeText == null || timeText.isEmpty()) {
            return referenceTime;
        }

        try {
            timeText = timeText.trim().toLowerCase();
            LocalDateTime now = LocalDateTime.now();
            
            // Extract number from text
            int amount = 1;
            String[] parts = timeText.split("\\s+");
            if (parts.length > 0) {
                try {
                    amount = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    // Extract first number from text
                    String numStr = parts[0].replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        amount = Integer.parseInt(numStr);
                    }
                }
            }

            // Parse time unit
            if (timeText.contains("second") || timeText.contains("s")) {
                return now.minusSeconds(amount);
            } else if (timeText.contains("minute") || timeText.contains("m")) {
                return now.minusMinutes(amount);
            } else if (timeText.contains("hour") || timeText.contains("h") || timeText.contains("giờ")) {
                return now.minusHours(amount);
            } else if (timeText.contains("day") || timeText.contains("d") || timeText.contains("ngày")) {
                return now.minusDays(amount);
            } else if (timeText.contains("week") || timeText.contains("w") || timeText.contains("tuần")) {
                return now.minusWeeks(amount);
            } else if (timeText.contains("month") || timeText.contains("mon") || timeText.contains("tháng")) {
                return now.minusMonths(amount);
            } else if (timeText.contains("year") || timeText.contains("y") || timeText.contains("năm")) {
                return now.minusYears(amount);
            } else if (timeText.contains("phút")) {
                return now.minusMinutes(amount);
            }
            
            // Default: return reference time if cannot parse
            return referenceTime;
        } catch (Exception e) {
            System.out.println("      ⚠️ Error parsing relative time '" + timeText + "': " + e.getMessage());
            return referenceTime;
        }
    }
}