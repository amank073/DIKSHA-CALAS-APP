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
        return subjectPart + input.topicName() + " " + examPart + "concept explanation";
    }

    private ContentOutput placeholder(ContentInput input) {
        String query = buildQuery(input);
        String searchUrl = "https://www.youtube.com/results?search_query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
        return new ContentOutput(input.topicName() + " — Concept Walkthrough", searchUrl);
    }

    @SuppressWarnings("unchecked")
    private ContentOutput tryYouTubeApi(ContentInput input) {
        try {
            String query = buildQuery(input);
            String url = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet&type=video&maxResults=1&order=relevance"
                    + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&key=" + youtubeApiKey;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) return null;

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items == null || items.isEmpty()) return null;

            Map<String, Object> first = items.get(0);
            Map<String, Object> id = (Map<String, Object>) first.get("id");
            Map<String, Object> snippet = (Map<String, Object>) first.get("snippet");
            if (id == null || snippet == null) return null;

            String videoId = (String) id.get("videoId");
            String title = (String) snippet.get("title");
            if (videoId == null) return null;

            return new ContentOutput(
                    title != null ? title : input.topicName(),
                    "https://www.youtube.com/watch?v=" + videoId
            );
        } catch (Exception e) {
            return null; // graceful fallback — see recommend()
        }
    }
}
