package com.humanitarian.devui.crawler;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.sentiment.EnhancedSentimentAnalyzer;
import com.humanitarian.devui.sentiment.SentimentAnalyzer;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*;

/**
 * YouTube Crawler - HTTP-based implementation using YouTube AJAX API
 * Logic ported directly from ytb_crawl.py (YoutubeCommentDownloader)
 * 
 * Key difference: Python uses raw regex with {.+?} pattern
 * Java uses Pattern.DOTALL to match . including newlines
 */
public class YouTubeCrawler implements DataCrawler {
    private HttpClient httpClient;
    private boolean initialized;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36";
    private static final String YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v={youtube_id}";
    
    // Regex patterns - EXACTLY from ytb_crawl.py but with proper Java escaping
    // Python: r'ytcfg\.set\s*\(\s*({.+?})\s*\)\s*;'
    // In Java: need to escape the curly braces as \{ and \} in the regex string
    private static final Pattern YT_CFG_RE = Pattern.compile(
        "ytcfg\\.set\\s*\\(\\s*(\\{.+?\\})\\s*\\)\\s*;", 
        Pattern.DOTALL
    );
    
    // Python: r'(?:window\s*\[\s*["\']ytInitialData["\']\s*\]|ytInitialData)\s*=\s*({.+?})\s*;\s*(?:var\s+meta|</script|\n)'
    private static final Pattern YT_INITIAL_DATA_RE = Pattern.compile(
        "(?:window\\s*\\[\\s*[\"']ytInitialData[\"']\\s*\\]|ytInitialData)\\s*=\\s*(\\{.+?\\})\\s*;\\s*(?:var\\s+meta|</script|\\n)",
        Pattern.DOTALL
    );
    
    private SentimentAnalyzer sentimentAnalyzer;

    public YouTubeCrawler() {
        this.initialized = false;
        this.httpClient = HttpClient.newHttpClient();
        this.sentimentAnalyzer = new EnhancedSentimentAnalyzer();
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    public void initialize() {
        System.out.println("🚀 Initializing YouTube Crawler (HTTP API)...");
        initialized = true;
        System.out.println("✓ YouTube Crawler initialized (HTTP mode)");
    }

    @Override
    public List<Post> crawlPosts(List<String> keywords, List<String> hashtags, int limit) {
        List<Post> allPosts = new ArrayList<>();
        
        List<String> searchTerms = new ArrayList<>();
        if (keywords != null) searchTerms.addAll(keywords);
        if (hashtags != null) {
            for (String tag : hashtags) {
                searchTerms.add(tag.startsWith("#") ? tag.substring(1) : tag);
            }
        }
        
        if (searchTerms.isEmpty()) {
            System.err.println("No search terms provided");
            return allPosts;
        }
        
        for (String term : searchTerms) {
            List<Post> results = crawlByKeyword(term, limit);
            allPosts.addAll(results);
            
            if (allPosts.size() >= limit) break;
        }
        
        return allPosts;
    }

    /**
     * Crawl YouTube videos by keyword - simplified for now
     */
    private List<Post> crawlByKeyword(String keyword, int limit) {
        List<Post> posts = new ArrayList<>();
        
        if (!initialized) {
            System.err.println("Crawler not initialized");
            return posts;
        }
        
        System.out.println("\n🎬 Crawling YouTube for keyword: " + keyword);
        // TODO: Implement video search
        
        return posts;
    }

    /**
     * Crawl single video by URL - main entry point
     * Implements: get_comments_from_url from Python
     */
    public YouTubePost crawlVideoByUrl(String videoUrl) {
        try {
            System.out.println("\n🔗 Crawling single video from URL");
            System.out.println("📍 URL: " + videoUrl);
            
            if (!videoUrl.contains("youtube.com")) {
                System.err.println("Invalid URL: Must be a YouTube URL");
                return null;
            }
            
            // Extract video ID from URL
            String videoId = extractVideoIdFromUrl(videoUrl);
            if (videoId == null || videoId.isEmpty()) {
                System.err.println("Could not extract video ID from URL");
                return null;
            }
            
            System.out.println("📝 Video ID: " + videoId);
            
            // Fetch the video page HTML
            System.out.println("⏳ Fetching video page...");
            String html = fetchPageContent(videoUrl);
            
            // Step 1: Extract ytcfg using regex_search (group 1 = the JSON)
            String ytcfgJson = regexSearch(html, YT_CFG_RE, 1, "");
            if (ytcfgJson.isEmpty()) {
                System.err.println("❌ Failed to extract ytcfg - regex didn't match");
                System.err.println("📄 HTML length: " + html.length() + " chars");
                // Debug: check if ytcfg.set is present
                if (html.contains("ytcfg.set")) {
                    System.err.println("⚠️ Found 'ytcfg.set' in HTML but regex didn't match");
                }
                return null;
            }
            
            System.out.println("✓ Extracted ytcfg (" + ytcfgJson.length() + " chars)");
            JSONObject ytcfg = new JSONObject(ytcfgJson);
            
            // Step 2: Extract ytInitialData using regex_search (group 1 = the JSON)
            String ytInitialDataJson = regexSearch(html, YT_INITIAL_DATA_RE, 1, "");
            if (ytInitialDataJson.isEmpty()) {
                System.err.println("❌ Failed to extract ytInitialData - regex didn't match");
                // Debug: check if ytInitialData is present
                if (html.contains("ytInitialData")) {
                    System.err.println("⚠️ Found 'ytInitialData' in HTML but regex didn't match");
                }
                return null;
            }
            
            System.out.println("✓ Extracted ytInitialData (" + ytInitialDataJson.length() + " chars)");
            JSONObject data = new JSONObject(ytInitialDataJson);
            
            // Extract video title
            String title = extractVideoTitle(html);
            if (title == null || title.isEmpty()) {
                title = "Video: " + videoId;
            }
            
            // Extract video publish date from ytInitialData
            LocalDateTime videoPublishDate = extractVideoPublishDate(data);
            if (videoPublishDate == null) {
                System.out.println("⚠️ Could not extract video publish date, using current date");
                videoPublishDate = LocalDateTime.now();
            } else {
                System.out.println("📅 Video publish date: " + videoPublishDate);
            }
            
            YouTubePost post = new YouTubePost(videoId, title, videoPublishDate, "YouTube User", videoUrl);
            
            // Step 3: Extract comments from the initial data
            // Look for itemSectionRenderer -> continuationItemRenderer
            System.out.println("💬 Extracting comments...");
            extractCommentsFromInitialData(post, data, ytcfg);
            
            System.out.println("✅ Successfully extracted video with " + post.getComments().size() + " comments");
            return post;
            
        } catch (Exception e) {
            System.err.println("❌ Error crawling video by URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract comments from ytInitialData
     * Equivalent to Python's get_comments_from_url logic
     */
    private void extractCommentsFromInitialData(YouTubePost post, JSONObject data, JSONObject ytcfg) {
        try {
            // Search for itemSectionRenderer
            List<JSONObject> itemSections = searchDict(data, "itemSectionRenderer");
            if (itemSections.isEmpty()) {
                System.out.println("⚠️ No itemSectionRenderer found");
                return;
            }
            
            JSONObject itemSection = itemSections.get(0);
            
            // Search for continuationItemRenderer in itemSection
            List<JSONObject> continuations = searchDict(itemSection, "continuationItemRenderer");
            if (continuations.isEmpty()) {
                System.out.println("⚠️ No continuationItemRenderer found - comments may be disabled");
                return;
            }
            
            // Extract the continuation endpoint
            JSONObject continuationRenderer = continuations.get(0);
            List<JSONObject> continuationEndpoints = searchDict(continuationRenderer, "continuationEndpoint");
            
            if (!continuationEndpoints.isEmpty()) {
                try {
                    JSONObject continuationEndpoint = continuationEndpoints.get(0);
                    JSONObject continuationCommand = continuationEndpoint.getJSONObject("continuationCommand");
                    String continuationToken = continuationCommand.getString("token");
                    
                    System.out.println("📌 Found continuation token, fetching comments...");
                    
                    // Build the AJAX request following Python's ajax_request
                    fetchCommentsWithContinuation(post, continuationToken, ytcfg);
                } catch (JSONException e) {
                    System.out.println("⚠️ Could not extract continuation token: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting comments from initial data: " + e.getMessage());
        }
    }

    /**
     * Fetch comments using continuation token via AJAX API
     * Equivalent to Python's ajax_request
     */
    private void fetchCommentsWithContinuation(YouTubePost post, String continuationToken, JSONObject ytcfg) {
        try {
            // Construct the API URL and request body like Python does
            String apiUrl = "https://www.youtube.com/youtubei/v1/next";
            String apiKey = ytcfg.getString("INNERTUBE_API_KEY");
            JSONObject context = ytcfg.getJSONObject("INNERTUBE_CONTEXT");
            
            // Build request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("context", context);
            requestBody.put("continuation", continuationToken);
            
            System.out.println("🔄 Making AJAX request to: " + apiUrl);
            
            // Make the HTTP POST request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(apiUrl + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .timeout(java.time.Duration.ofSeconds(60))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JSONObject responseData = new JSONObject(response.body());
                extractCommentsFromResponse(post, responseData);
            } else if (response.statusCode() == 403 || response.statusCode() == 413) {
                System.out.println("⚠️ API rate limited (status " + response.statusCode() + ")");
            } else {
                System.err.println("⚠️ AJAX request failed with status " + response.statusCode());
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching comments with continuation: " + e.getMessage());
        }
    }

    /**
     * Extract comments from AJAX response
     */
    private void extractCommentsFromResponse(YouTubePost post, JSONObject response) {
        try {
            // Search for commentEntityPayload in the response
            List<JSONObject> commentPayloads = searchDict(response, "commentEntityPayload");
            
            System.out.println("Found " + commentPayloads.size() + " comments in response");
            
            for (JSONObject payload : commentPayloads) {
                try {
                    JSONObject properties = payload.getJSONObject("properties");
                    JSONObject author = payload.getJSONObject("author");
                    
                    String commentId = properties.getString("commentId");
                    String content = properties.getJSONObject("content").getString("content");
                    String authorName = author.getString("displayName");
                    
                    // Extract comment creation date/time
                    LocalDateTime commentDate = extractCommentDateTime(properties);
                    if (commentDate == null) {
                        // Fallback to video publish date if available
                        commentDate = post.getCreatedAt();
                    }
                    
                    Comment comment = new Comment(
                        commentId,
                        post.getPostId(),
                        content,
                        commentDate,
                        authorName
                    );
                    
                    // Set sentiment if analyzer available
                    if (sentimentAnalyzer != null) {
                        Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(content);
                        comment.setSentiment(sentiment);
                    }
                    
                    post.addComment(comment);
                    
                } catch (JSONException e) {
                    // Skip malformed comments
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting comments from response: " + e.getMessage());
        }
    }

    /**
     * Fetch page content using HTTP GET
     */
    private String fetchPageContent(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(java.net.URI.create(url))
            .header("User-Agent", USER_AGENT)
            .timeout(java.time.Duration.ofSeconds(30))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return response.body();
        }
        
        throw new Exception("Failed to fetch page: HTTP " + response.statusCode());
    }

    /**
     * Extract video publish date from ytInitialData
     * YouTube embeds date info in various places - we'll search for date patterns in JSON
     */
    private LocalDateTime extractVideoPublishDate(JSONObject data) {
        try {
            String jsonStr = data.toString();
            
            // Pattern 1: Look for Vietnamese month format "thg XX, YYYY"
            Pattern vietnamesePattern = Pattern.compile(
                "\"(?:dateText|publishedTimeText|uploadDate)\"\\s*:\\s*\\{[^}]*\"simpleText\"\\s*:\\s*\"([^\"]*thg[^\"]+)\"",
                Pattern.CASE_INSENSITIVE
            );
            Matcher matcher = vietnamesePattern.matcher(jsonStr);
            
            if (matcher.find()) {
                String dateStr = matcher.group(1);
                System.out.println("  📝 Found Vietnamese date in JSON: " + dateStr);
                
                LocalDateTime date = parseYouTubeDateString(dateStr);
                if (date != null) {
                    System.out.println("✓ Extracted video date: " + dateStr + " → " + date);
                    return date;
                }
            }
            
            // Pattern 2: Look for "Jan 15, 2025" or similar date formats in JSON values
            Pattern datePattern = Pattern.compile(
                "\"(?:dateText|publishedTimeText|uploadDate)\"\\s*:\\s*\\{[^}]*\"simpleText\"\\s*:\\s*\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE
            );
            matcher = datePattern.matcher(jsonStr);
            
            while (matcher.find()) {
                String dateStr = matcher.group(1);
                System.out.println("  📝 Found in JSON: " + dateStr);
                
                if (!dateStr.contains("ago") && !dateStr.contains("view")) {
                    LocalDateTime date = parseYouTubeDateString(dateStr);
                    if (date != null) {
                        System.out.println("✓ Extracted video date: " + dateStr + " → " + date);
                        return date;
                    }
                }
            }
            
            // Pattern 3: Look for ISO date format in uploadDate fields
            Pattern isoDatePattern = Pattern.compile("\"uploadDate\"\\s*:\\s*\"(\\d{4}-\\d{2}-\\d{2})");
            matcher = isoDatePattern.matcher(jsonStr);
            
            while (matcher.find()) {
                String dateStr = matcher.group(1);
                System.out.println("  📝 Found ISO date: " + dateStr);
                
                LocalDateTime date = parseYouTubeDateString(dateStr);
                if (date != null) {
                    System.out.println("✓ Extracted video date: " + dateStr + " → " + date);
                    return date;
                }
            }
            
            // Pattern 4: Search for common date formats in the entire JSON
            Pattern commonDatePattern = Pattern.compile(
                "thg\\s+\\d{1,2},?\\s+\\d{4}|[A-Za-z]+\\s+\\d{1,2},?\\s+\\d{4}|\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4}|\\d{4}-\\d{2}-\\d{2}"
            );
            matcher = commonDatePattern.matcher(jsonStr);
            
            int count = 0;
            while (matcher.find() && count < 5) { // Limit to first 5 matches
                String dateStr = matcher.group(0);
                System.out.println("  📝 Found potential date: " + dateStr);
                count++;
                
                // Try to parse
                LocalDateTime date = parseYouTubeDateString(dateStr);
                if (date != null) {
                    System.out.println("✓ Extracted video date: " + dateStr + " → " + date);
                    return date;
                }
            }
            
            // Fallback: Search using the original searchDict approach
            List<JSONObject> dateTexts = searchDict(data, "dateText");
            System.out.println("🔍 Found " + dateTexts.size() + " dateText objects via searchDict");
            
            for (JSONObject dateObj : dateTexts) {
                try {
                    if (dateObj.has("simpleText")) {
                        String dateStr = dateObj.getString("simpleText");
                        System.out.println("  📝 Found dateText: " + dateStr);
                        
                        // Skip if it's a duration or view count format
                        if (dateStr.contains("ago") || dateStr.contains("views") || dateStr.contains("ago")) {
                            System.out.println("    ⏭️ Skipping (relative time or view count)");
                            continue;
                        }
                        
                        LocalDateTime date = parseYouTubeDateString(dateStr);
                        if (date != null) {
                            System.out.println("✓ Extracted video date: " + dateStr + " → " + date);
                            return date;
                        }
                    }
                } catch (JSONException e) {
                    // Continue to next
                }
            }
            
            // Alternative: Search for "publishedTimeText"
            List<JSONObject> publishedTexts = searchDict(data, "publishedTimeText");
            System.out.println("🔍 Found " + publishedTexts.size() + " publishedTimeText objects");
            
            for (JSONObject pubObj : publishedTexts) {
                try {
                    if (pubObj.has("simpleText")) {
                        String dateStr = pubObj.getString("simpleText");
                        System.out.println("  📝 Found publishedTimeText: " + dateStr);
                        
                        LocalDateTime date = parseYouTubeDateString(dateStr);
                        if (date != null) {
                            System.out.println("✓ Extracted video date from publishedTimeText: " + dateStr + " → " + date);
                            return date;
                        }
                    }
                } catch (JSONException e) {
                    // Continue
                }
            }
            
            // Fallback: search for uploadDate in JSON-LD structured data
            List<JSONObject> uploadDates = searchDict(data, "uploadDate");
            System.out.println("🔍 Found " + uploadDates.size() + " uploadDate objects");
            
            for (JSONObject uploadDateObj : uploadDates) {
                try {
                    String dateStr = uploadDateObj.toString();
                    if (dateStr.length() < 100) { // Avoid long JSON objects
                        System.out.println("  📝 Found uploadDate: " + dateStr);
                        LocalDateTime date = parseYouTubeDateString(dateStr);
                        if (date != null) {
                            System.out.println("✓ Extracted video date from uploadDate: " + dateStr + " → " + date);
                            return date;
                        }
                    }
                } catch (Exception e) {
                    // Continue
                }
            }
            
            System.out.println("⚠️ No valid date found in ytInitialData");
            return null;
        } catch (Exception e) {
            System.err.println("Error extracting video publish date: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extract comment creation date from properties
     * YouTube API provides "createTime" or relative time like "2 days ago"
     */
    private LocalDateTime extractCommentDateTime(JSONObject properties) {
        try {
            // Try to get createTime first (Unix timestamp in seconds or milliseconds)
            if (properties.has("createTime")) {
                try {
                    long timestamp = properties.getLong("createTime");
                    // Convert to LocalDateTime (assuming seconds)
                    if (timestamp > 0) {
                        return java.time.Instant.ofEpochSecond(timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();
                    }
                } catch (JSONException e) {
                    // Not a long, try other formats
                }
            }
            
            // Try publishedTimeText (like "2 days ago", "1 year ago", etc)
            if (properties.has("publishedTimeText")) {
                try {
                    JSONObject timeText = properties.getJSONObject("publishedTimeText");
                    if (timeText.has("simpleText")) {
                        String timeStr = timeText.getString("simpleText");
                        LocalDateTime date = parseYouTubeRelativeTime(timeStr);
                        if (date != null) {
                            System.out.println("✓ Parsed comment time: " + timeStr);
                            return date;
                        }
                    }
                } catch (JSONException e) {
                    // Continue
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error extracting comment date: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse YouTube date strings like "Jan 15, 2023" or "Uploaded on Dec 25, 2024"
     */
    private LocalDateTime parseYouTubeDateString(String dateStr) {
        try {
            // Remove common prefixes
            dateStr = dateStr.replaceAll("(Uploaded on|Streamed|Started streaming|Published|Premiere)\\s*", "");
            dateStr = dateStr.trim();
            
            // Handle Vietnamese month abbreviation "thg" (tháng)
            // E.g., "thg 12, 2024" -> "Dec 1, 2024"
            if (dateStr.toLowerCase().contains("thg")) {
                String[] viMonths = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("thg\\s+(\\d+),?\\s+(\\d{4})");
                java.util.regex.Matcher matcher = pattern.matcher(dateStr);
                
                if (matcher.find()) {
                    int month = Integer.parseInt(matcher.group(1));
                    int year = Integer.parseInt(matcher.group(2));
                    
                    if (month >= 1 && month <= 12) {
                        String englishDate = viMonths[month] + " 1, " + year; // Day 1 of the month
                        System.out.println("  🌍 Converted Vietnamese date: " + dateStr + " → " + englishDate);
                        dateStr = englishDate;
                    }
                }
            }
            
            // Try various date formats
            String[] formats = {
                "MMM dd, yyyy",           // Jan 15, 2023
                "MMM d, yyyy",            // Jan 5, 2023
                "MMMM dd, yyyy",          // January 15, 2023
                "MMMM d, yyyy",           // January 5, 2023
                "yyyy-MM-dd",             // 2023-01-15 (ISO format)
                "MMM dd yyyy",            // Jan 15 2023 (no comma)
                "MMM d yyyy",             // Jan 5 2023 (no comma)
                "MMM dd",                 // Jan 15 (no year)
                "MMMM d"                  // January 15 (no year)
            };
            
            for (String format : formats) {
                try {
                    java.time.format.DateTimeFormatter formatter = 
                        java.time.format.DateTimeFormatter.ofPattern(format);
                    
                    // If format has no year, add current year
                    String dateToparse = dateStr;
                    if (!format.contains("yyyy")) {
                        dateToparse = dateStr + ", " + java.time.Year.now().getValue();
                    }
                    
                    java.time.LocalDate date = java.time.LocalDate.parse(dateToparse, formatter);
                    return date.atStartOfDay();
                } catch (Exception e) {
                    // Try next format
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse YouTube relative time strings like "2 days ago", "1 month ago", "1 year ago"
     */
    private LocalDateTime parseYouTubeRelativeTime(String timeStr) {
        try {
            timeStr = timeStr.toLowerCase().trim();
            
            // Parse patterns like "2 days ago", "1 month ago", etc
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*(second|minute|hour|day|week|month|year)s?\\s*ago");
            java.util.regex.Matcher matcher = pattern.matcher(timeStr);
            
            if (matcher.find()) {
                int amount = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);
                
                LocalDateTime now = LocalDateTime.now();
                
                switch (unit) {
                    case "second":
                        return now.minusSeconds(amount);
                    case "minute":
                        return now.minusMinutes(amount);
                    case "hour":
                        return now.minusHours(amount);
                    case "day":
                        return now.minusDays(amount);
                    case "week":
                        return now.minusWeeks(amount);
                    case "month":
                        return now.minusMonths(amount);
                    case "year":
                        return now.minusYears(amount);
                    default:
                        return null;
                }
            }
            
            // Handle "Today", "Yesterday", etc
            if (timeStr.equals("today")) {
                return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            }
            if (timeStr.equals("yesterday")) {
                return LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Regex search - equivalent to Python's regex_search method
     * Returns group(groupNum) if match found, else default
     */
    private static String regexSearch(String text, Pattern pattern, int group, String defaultValue) {
        try {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(group);
            }
        } catch (Exception e) {
            System.err.println("Regex search error: " + e.getMessage());
        }
        return defaultValue;
    }

    /**
     * Recursive search for key in JSON object - equivalent to Python's search_dict
     * Uses stack-based approach (not true recursion) to avoid stack overflow
     * 
     * Python code:
     * @staticmethod
     * def search_dict(partial, search_key):
     *     stack = [partial]
     *     while stack:
     *         current_item = stack.pop()
     *         if isinstance(current_item, dict):
     *             for key, value in current_item.items():
     *                 if key == search_key:
     *                     yield value
     *                 else:
     *                     stack.append(value)
     *         elif isinstance(current_item, list):
     *             stack.extend(current_item)
     */
    private static List<JSONObject> searchDict(Object partial, String searchKey) {
        List<JSONObject> results = new ArrayList<>();
        Stack<Object> stack = new Stack<>();
        
        if (partial != null) {
            stack.push(partial);
        }
        
        while (!stack.isEmpty()) {
            Object currentItem = stack.pop();
            
            if (currentItem instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) currentItem;
                Iterator<String> keys = jsonObj.keys();
                
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonObj.opt(key);
                    
                    if (key.equals(searchKey)) {
                        // Found a matching key
                        if (value instanceof JSONObject) {
                            results.add((JSONObject) value);
                        }
                    } else if (value != null) {
                        // Push to stack to continue searching
                        stack.push(value);
                    }
                }
            } else if (currentItem instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) currentItem;
                
                // Add all array items to stack (in reverse order to maintain order)
                for (int i = jsonArray.length() - 1; i >= 0; i--) {
                    Object item = jsonArray.opt(i);
                    if (item != null) {
                        stack.push(item);
                    }
                }
            }
        }
        
        return results;
    }

    /**
     * Extract video ID from YouTube URL
     */
    private String extractVideoIdFromUrl(String url) {
        Pattern pattern = Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([A-Za-z0-9_\\-]+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Extract video title from page HTML
     */
    private String extractVideoTitle(String html) {
        try {
            // Try to extract from meta og:title tag
            Pattern metaPattern = Pattern.compile("<meta\\s+property=[\"']og:title[\"']\\s+content=[\"']([^\"']+)[\"']");
            Matcher matcher = metaPattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
            // Fallback: extract from meta title tag
            metaPattern = Pattern.compile("<meta\\s+name=[\"']title[\"']\\s+content=[\"']([^\"']+)[\"']");
            matcher = metaPattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting title: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getCrawlerName() {
        return "YouTubeCrawler (HTTP API)";
    }

    @Override
    public void shutdown() {
        System.out.println("YouTube Crawler shutdown");
        initialized = false;
    }
}
