package com.diksha.service.engine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.diksha.service.engine.Contracts.ContentInput;
import static com.diksha.service.engine.Contracts.ContentOutput;

/**
 * PLUGGABLE ENGINE — recommends a video for a scheduled topic.
 * <p>
 * <b>To plug in a real YouTube Data API v3 key:</b> set the property
 * {@code youtube.api.key} (in application.properties, or the
 * {@code YOUTUBE_API_KEY} environment variable — Spring's relaxed
 * binding maps it automatically). When present, this engine calls
 * YouTube's {@code search.list} endpoint and uses the first real result's
 * title + link. Get a key from
 * <a href="https://console.cloud.google.com/apis/credentials">Google Cloud Console</a>
 * (enable "YouTube Data API v3" on the project first) — free tier is
 * 10,000 quota units/day, and a search call costs 100 units.
 * <p>
 * Without a key (the default), this falls back to a deterministic YouTube
 * *search results* URL built from the topic/subject/exam names — it
 * always resolves to a real YouTube search page, just not one specific
 * pre-verified video.
 */
@Component
public class ContentRecommender {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.api.key:}")
    private String youtubeApiKey;

    public boolean isConfigured() {
        return youtubeApiKey != null && !youtubeApiKey.isBlank();
    }

    public ContentOutput recommend(ContentInput input) {
        if (isConfigured()) {
            ContentOutput fromApi = tryYouTubeApi(input);
            if (fromApi != null) {
                return fromApi;
            }
            // Falls through to the placeholder below on any API error
            // (quota exceeded, network issue, bad key, ...) so plan
            // generation never fails just because content lookup did.
        }
        return placeholder(input);
    }

    private String buildQuery(ContentInput input) {
        String subjectPart = input.subjectName() != null ? input.subjectName() + " " : "";
        String examPart = input.examType() != null ? input.examType() + " " : "";
        // Clean up exam part if it has weird parentheses that break search
        if (examPart.contains("(")) {
            examPart = examPart.substring(0, examPart.indexOf("(")).trim() + " ";
        }
        return subjectPart + input.topicName() + " full lecture -\"one shot\" -oneshot";
    }

    private ContentOutput placeholder(ContentInput input) {
        String query = buildQuery(input);
        String searchUrl = "https://www.youtube.com/results?search_query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
        return new ContentOutput(input.topicName() + " — Concept Walkthrough", searchUrl, "", "");
    }

    public List<ContentOutput> recommendList(ContentInput input) {
        if (isConfigured()) {
            List<ContentOutput> fromApi = tryYouTubeApiList(input);
            if (fromApi != null && !fromApi.isEmpty()) {
                return fromApi;
            }
        }
        return List.of(placeholder(input));
    }

    @SuppressWarnings("unchecked")
    private List<ContentOutput> tryYouTubeApiList(ContentInput input) {
        try {
            String query = buildQuery(input);
            // using videoDuration=long to bias towards comprehensive lectures (> 20 mins)
            String urlStr = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet&type=video&maxResults=8&order=relevance&videoDuration=long"
                    + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&key=" + youtubeApiKey;

            java.net.URI uri = java.net.URI.create(urlStr);
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null) return null;

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items == null || items.isEmpty()) return null;

            List<ContentOutput> results = new java.util.ArrayList<>();
            for (Map<String, Object> item : items) {
                Map<String, Object> id = (Map<String, Object>) item.get("id");
                Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                if (id == null || snippet == null) continue;

                String videoId = (String) id.get("videoId");
                String title = (String) snippet.get("title");
                String channelTitle = (String) snippet.get("channelTitle");
                String thumbnailUrl = "";
                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                if (thumbnails != null) {
                    Map<String, Object> medium = (Map<String, Object>) thumbnails.get("medium");
                    if (medium != null) {
                        thumbnailUrl = (String) medium.get("url");
                    } else {
                        Map<String, Object> def = (Map<String, Object>) thumbnails.get("default");
                        if (def != null) {
                            thumbnailUrl = (String) def.get("url");
                        }
                    }
                }
                
                if (videoId != null) {
                    // HTML unescape title for cleaner display
                    String cleanTitle = title != null ? title.replace("&quot;", "\"").replace("&#39;", "'").replace("&amp;", "&") : input.topicName();
                    results.add(new ContentOutput(cleanTitle, "https://www.youtube.com/watch?v=" + videoId, thumbnailUrl, channelTitle != null ? channelTitle : ""));
                }
            }
            return results;
        } catch (Exception e) {
            System.err.println("YouTube API failed: " + e.getMessage());
            return null; // return null to trigger the placeholder fallback
        }
    }

    @SuppressWarnings("unchecked")
    private ContentOutput tryYouTubeApi(ContentInput input) {
        List<ContentOutput> list = tryYouTubeApiList(input);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
