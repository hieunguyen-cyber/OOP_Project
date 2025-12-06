package com.humanitarian.logistics.crawler;

import com.google.gson.*;
import com.humanitarian.logistics.model.Comment;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public class YouTubeAPIHelper {
    private final String apiKey;
    private static final String API_BASE = "https://www.googleapis.com/youtube/v3";

    public YouTubeAPIHelper(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> searchVideos(String query, int maxResults) throws Exception {
        OkHttpClient client = new OkHttpClient();
        List<String> videoIds = new ArrayList<>();

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        String url = API_BASE + "/search?" +
                "part=snippet&" +
                "type=video&" +
                "maxResults=" + maxResults + "&" +
                "q=" + encodedQuery + "&" +
                "key=" + apiKey;

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                JsonArray items = json.getAsJsonArray("items");

                if (items != null) {
                    for (JsonElement item : items) {
                        try {
                            String videoId = item.getAsJsonObject()
                                    .getAsJsonObject("id")
                                    .get("videoId")
                                    .getAsString();
                            videoIds.add(videoId);
                        } catch (Exception e) {

                        }
                    }
                }
            }
        }

        return videoIds;
    }

    public List<Comment> getComments(String videoId) throws Exception {
        OkHttpClient client = new OkHttpClient();
        List<Comment> comments = new ArrayList<>();
        String pageToken = "";
        boolean hasNext = true;

        while (hasNext) {
            String url = API_BASE + "/commentThreads?" +
                    "part=snippet&" +
                    "videoId=" + videoId + "&" +
                    "maxResults=100&" +
                    "key=" + apiKey;

            if (!pageToken.isEmpty()) {
                url += "&pageToken=" + pageToken;
            }

            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    break;
                }

                String jsonData = response.body().string();
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                JsonArray items = json.getAsJsonArray("items");

                if (items != null) {
                    for (JsonElement item : items) {
                        try {
                            JsonObject snippet = item.getAsJsonObject()
                                    .getAsJsonObject("snippet")
                                    .getAsJsonObject("topLevelComment")
                                    .getAsJsonObject("snippet");

                            String author = snippet.get("authorDisplayName").getAsString();
                            String text = snippet.get("textOriginal").getAsString();
                            String publishedAt = snippet.get("publishedAt").getAsString();

                            LocalDateTime createdAt = parseISO8601(publishedAt);
                            
                            Comment comment = new Comment(
                                    UUID.randomUUID().toString(),
                                    videoId,
                                    text,
                                    createdAt,
                                    author
                            );
                            comments.add(comment);
                        } catch (Exception e) {

                        }
                    }
                }

                if (json.has("nextPageToken")) {
                    pageToken = json.get("nextPageToken").getAsString();
                } else {
                    hasNext = false;
                }
            }
        }

        return comments;
    }

    public JsonObject getVideoDetails(String videoId) throws Exception {
        OkHttpClient client = new OkHttpClient();

        String url = API_BASE + "/videos?" +
                "part=snippet,contentDetails&" +
                "id=" + videoId + "&" +
                "key=" + apiKey;

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
                JsonArray items = json.getAsJsonArray("items");

                if (items != null && items.size() > 0) {
                    return items.get(0).getAsJsonObject().getAsJsonObject("snippet");
                }
            }
        }

        return null;
    }

    private LocalDateTime parseISO8601(String dateStr) {
        try {

            dateStr = dateStr.replace("Z", "+00:00");
            
            return java.time.OffsetDateTime.parse(dateStr).toLocalDateTime();
        } catch (Exception e) {
            System.err.println("Could not parse date: " + dateStr);
            return LocalDateTime.now();
        }
    }

    public boolean isAPIKeyValid() {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }

        try {
            OkHttpClient client = new OkHttpClient();
            String url = API_BASE + "/search?part=snippet&q=test&maxResults=1&key=" + apiKey;
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
