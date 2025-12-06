package com.humanitarian.logistics.crawler;

import com.humanitarian.logistics.model.*;
import com.humanitarian.logistics.sentiment.EnhancedSentimentAnalyzer;
import com.humanitarian.logistics.sentiment.SentimentAnalyzer;

import net.bytebuddy.asm.MemberSubstitution.Substitution.Chain.Step.ForField.Write;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*;

public class YouTubeCrawler implements DataCrawler {
    private HttpClient httpClient;
    private boolean initialized;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36";
    private static final String YOUTUBE_VIDEO_URL = "https://www.youtube.com/watch?v={youtube_id}";
    
    private static final Pattern YT_CFG_RE = Pattern.compile(
        "ytcfg\\.set\\s*\\(\\s*(\\{.+?\\})\\s*\\)\\s*;", 
        Pattern.DOTALL
    );
    
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

    private List<Post> crawlByKeyword(String keyword, int limit) {
        List<Post> posts = new ArrayList<>();
        
        if (!initialized) {
            System.err.println("Crawler not initialized");
            return posts;
        }
        
        System.out.println("\n🎬 Crawling YouTube for keyword: " + keyword);
        try {
            String searchUrl = "https://www.youtube.com/results?search_query=" 
                                + java.net.URLEncoder.encode(keyword, "UTF-8");

            System.out.println("🔎 Searching YouTube: " + searchUrl);

            String html = fetchPageContent(searchUrl);

            Pattern videoIdPattern = Pattern.compile("\"videoId\":\"([A-Za-z0-9_-]{11})\"");
            Matcher matcher = videoIdPattern.matcher(html);

            Set<String> videoIds = new LinkedHashSet<>();

            while (matcher.find() && videoIds.size() < limit) {
                videoIds.add(matcher.group(1));
            }

            if (videoIds.isEmpty()) {
                System.out.println("⚠️ No videos found for keyword: " + keyword);
                return posts;
            }

            System.out.println("📌 Found " + videoIds.size() + " video IDs for keyword: " + keyword);

            for (String vid : videoIds) {
                if (posts.size() >= limit) break;

                String videoUrl = "https://www.youtube.com/watch?v=" + vid;
                System.out.println("➡️ Crawling video: " + videoUrl);

                YouTubePost ytPost = crawlVideoByUrl(videoUrl);
                if (ytPost != null) {
                    posts.add(ytPost);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error during keyword search: " + e.getMessage());
            e.printStackTrace();
        }

        return posts;
    }

    public YouTubePost crawlVideoByUrl(String videoUrl) {
        try {
            System.out.println("\n🔗 Crawling single video from URL");
            System.out.println("📍 URL: " + videoUrl);
            
            if (!videoUrl.contains("youtube.com")) {
                System.err.println("Invalid URL: Must be a YouTube URL");
                return null;
            }
            
            String videoId = extractVideoIdFromUrl(videoUrl);
            if (videoId == null || videoId.isEmpty()) {
                System.err.println("Could not extract video ID from URL");
                return null;
            }
            
            System.out.println("📝 Video ID: " + videoId);
            
            System.out.println("⏳ Fetching video page...");
            String html = fetchPageContent(videoUrl);
            
            String ytcfgJson = regexSearch(html, YT_CFG_RE, 1, "");
            if (ytcfgJson.isEmpty()) {
                System.err.println("❌ Failed to extract ytcfg - regex didn't match");
                System.err.println("📄 HTML length: " + html.length() + " chars");

                if (html.contains("ytcfg.set")) {
                    System.err.println("⚠️ Found 'ytcfg.set' in HTML but regex didn't match");
                }
                return null;
            }
            
            System.out.println("✓ Extracted ytcfg (" + ytcfgJson.length() + " chars)");
            JSONObject ytcfg = new JSONObject(ytcfgJson);
            
            String ytInitialDataJson = regexSearch(html, YT_INITIAL_DATA_RE, 1, "");
            if (ytInitialDataJson.isEmpty()) {
                System.err.println("❌ Failed to extract ytInitialData - regex didn't match");

                if (html.contains("ytInitialData")) {
                    System.err.println("⚠️ Found 'ytInitialData' in HTML but regex didn't match");
                }
                return null;
            }
            
            System.out.println("✓ Extracted ytInitialData (" + ytInitialDataJson.length() + " chars)");
            JSONObject data = new JSONObject(ytInitialDataJson);
            
            String title = extractVideoTitle(html);
            if (title == null || title.isEmpty()) {
                title = "Video: " + videoId;
            }
            
            LocalDateTime videoPublishDate = extractVideoPublishDate(data);
            if (videoPublishDate == null) {
                System.out.println("⚠️ Could not extract video publish date, using current date");
                videoPublishDate = LocalDateTime.now();
            } else {
                System.out.println("📅 Video publish date: " + videoPublishDate);
            }
            
            YouTubePost post = new YouTubePost(videoId, title, videoPublishDate, "YouTube User", videoUrl);
            
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

    private void extractCommentsFromInitialData(YouTubePost post, JSONObject data, JSONObject ytcfg) {
        try {

            List<JSONObject> itemSections = searchDict(data, "itemSectionRenderer");
            if (itemSections.isEmpty()) {
                System.out.println("⚠️ No itemSectionRenderer found");
                return;
            }
            
            JSONObject itemSection = itemSections.get(0);
            
            List<JSONObject> continuations = searchDict(itemSection, "continuationItemRenderer");
            if (continuations.isEmpty()) {
                System.out.println("⚠️ No continuationItemRenderer found - comments may be disabled");
                return;
            }
            
            JSONObject continuationRenderer = continuations.get(0);
            List<JSONObject> continuationEndpoints = searchDict(continuationRenderer, "continuationEndpoint");
            
            if (!continuationEndpoints.isEmpty()) {
                try {
                    JSONObject continuationEndpoint = continuationEndpoints.get(0);
                    JSONObject continuationCommand = continuationEndpoint.getJSONObject("continuationCommand");
                    String continuationToken = continuationCommand.getString("token");
                    
                    System.out.println("📌 Found continuation token, fetching comments...");
                    
                    fetchCommentsWithContinuation(post, continuationToken, ytcfg);
                } catch (JSONException e) {
                    System.out.println("⚠️ Could not extract continuation token: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting comments from initial data: " + e.getMessage());
        }
    }

    private void fetchCommentsWithContinuation(YouTubePost post, String continuationToken, JSONObject ytcfg) {
        try {

            String apiUrl = "https://www.youtube.com/youtubei/v1/next";
            String apiKey = ytcfg.getString("INNERTUBE_API_KEY");
            JSONObject context = ytcfg.getJSONObject("INNERTUBE_CONTEXT");
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("context", context);
            requestBody.put("continuation", continuationToken);
            
            System.out.println("🔄 Making AJAX request to: " + apiUrl);
            
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

    private void extractCommentsFromResponse(YouTubePost post, JSONObject response) {
        try {

            List<JSONObject> commentPayloads = searchDict(response, "commentEntityPayload");
            
            System.out.println("Found " + commentPayloads.size() + " comments in response");
            
            for (JSONObject payload : commentPayloads) {
                try {
                    JSONObject properties = payload.getJSONObject("properties");
                    JSONObject author = payload.getJSONObject("author");
                    
                    String commentId = properties.getString("commentId");
                    String content = properties.getJSONObject("content").getString("content");
                    String authorName = author.getString("displayName");
                    
                    LocalDateTime commentDate = extractCommentDateTime(properties);
                    if (commentDate == null) {

                        commentDate = post.getCreatedAt();
                    }
                    
                    Comment comment = new Comment(
                        commentId,
                        post.getPostId(),
                        content,
                        commentDate,
                        authorName
                    );
                    
                    if (sentimentAnalyzer != null) {
                        Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(content);
                        comment.setSentiment(sentiment);
                    }
                    
                    post.addComment(comment);
                    
                } catch (JSONException e) {

                }
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting comments from response: " + e.getMessage());
        }
    }

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

    private LocalDateTime extractVideoPublishDate(JSONObject data) {
        try {
            String jsonStr = data.toString();
            
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
            
            Pattern commonDatePattern = Pattern.compile(
                "thg\\s+\\d{1,2},?\\s+\\d{4}|[A-Za-z]+\\s+\\d{1,2},?\\s+\\d{4}|\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4}|\\d{4}-\\d{2}-\\d{2}"
            );
            matcher = commonDatePattern.matcher(jsonStr);
            
            int count = 0;
            while (matcher.find() && count < 5) {
                String dateStr = matcher.group(0);
                System.out.println("  📝 Found potential date: " + dateStr);
                count++;
                
                LocalDateTime date = parseYouTubeDateString(dateStr);
                if (date != null) {
                    System.out.println("✓ Extracted video date: " + dateStr + " → " + date);
                    return date;
                }
            }
            
            List<JSONObject> dateTexts = searchDict(data, "dateText");
            System.out.println("🔍 Found " + dateTexts.size() + " dateText objects via searchDict");
            
            for (JSONObject dateObj : dateTexts) {
                try {
                    if (dateObj.has("simpleText")) {
                        String dateStr = dateObj.getString("simpleText");
                        System.out.println("  📝 Found dateText: " + dateStr);
                        
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

                }
            }
            
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

                }
            }
            
            List<JSONObject> uploadDates = searchDict(data, "uploadDate");
            System.out.println("🔍 Found " + uploadDates.size() + " uploadDate objects");
            
            for (JSONObject uploadDateObj : uploadDates) {
                try {
                    String dateStr = uploadDateObj.toString();
                    if (dateStr.length() < 100) {
                        System.out.println("  📝 Found uploadDate: " + dateStr);
                        LocalDateTime date = parseYouTubeDateString(dateStr);
                        if (date != null) {
                            System.out.println("✓ Extracted video date from uploadDate: " + dateStr + " → " + date);
                            return date;
                        }
                    }
                } catch (Exception e) {

                }
            }
            
            System.out.println("⚠️ No valid date found in ytInitialData");
            return null;
        } catch (Exception e) {
            System.err.println("Error extracting video publish date: " + e.getMessage());
            return null;
        }
    }

    private LocalDateTime extractCommentDateTime(JSONObject properties) {
        try {

            if (properties.has("createTime")) {
                try {
                    long timestamp = properties.getLong("createTime");

                    if (timestamp > 0) {
                        return java.time.Instant.ofEpochSecond(timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime();
                    }
                } catch (JSONException e) {

                }
            }
            
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

                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error extracting comment date: " + e.getMessage());
            return null;
        }
    }

    private LocalDateTime parseYouTubeDateString(String dateStr) {
        try {

            dateStr = dateStr.replaceAll("(Uploaded on|Streamed|Started streaming|Published|Premiere)\\s*", "");
            dateStr = dateStr.trim();
            
            if (dateStr.toLowerCase().contains("thg")) {
                String[] viMonths = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("thg\\s+(\\d+),?\\s+(\\d{4})");
                java.util.regex.Matcher matcher = pattern.matcher(dateStr);
                
                if (matcher.find()) {
                    int month = Integer.parseInt(matcher.group(1));
                    int year = Integer.parseInt(matcher.group(2));
                    
                    if (month >= 1 && month <= 12) {
                        String englishDate = viMonths[month] + " 1, " + year;
                        System.out.println("  🌍 Converted Vietnamese date: " + dateStr + " → " + englishDate);
                        dateStr = englishDate;
                    }
                }
            }
            
            String[] formats = {
                "MMM dd, yyyy",
                "MMM d, yyyy",
                "MMMM dd, yyyy",
                "MMMM d, yyyy",
                "yyyy-MM-dd",
                "MMM dd yyyy",
                "MMM d yyyy",
                "MMM dd",
                "MMMM d"
            };
            
            for (String format : formats) {
                try {
                    java.time.format.DateTimeFormatter formatter = 
                        java.time.format.DateTimeFormatter.ofPattern(format);
                    
                    String dateToparse = dateStr;
                    if (!format.contains("yyyy")) {
                        dateToparse = dateStr + ", " + java.time.Year.now().getValue();
                    }
                    
                    java.time.LocalDate date = java.time.LocalDate.parse(dateToparse, formatter);
                    return date.atStartOfDay();
                } catch (Exception e) {

                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseYouTubeRelativeTime(String timeStr) {
        try {
            timeStr = timeStr.toLowerCase().trim();
            
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

                        if (value instanceof JSONObject) {
                            results.add((JSONObject) value);
                        }
                    } else if (value != null) {

                        stack.push(value);
                    }
                }
            } else if (currentItem instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) currentItem;
                
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

    private String extractVideoIdFromUrl(String url) {
        Pattern pattern = Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([A-Za-z0-9_\\-]+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractVideoTitle(String html) {
        try {

            Pattern metaPattern = Pattern.compile("<meta\\s+property=[\"']og:title[\"']\\s+content=[\"']([^\"']+)[\"']");
            Matcher matcher = metaPattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
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
