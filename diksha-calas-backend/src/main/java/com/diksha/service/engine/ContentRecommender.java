package com.diksha.service.engine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.diksha.service.engine.Contracts.ContentInput;
import static com.diksha.service.engine.Contracts.ContentOutput;

/**
 * PLUGGABLE ENGINE — recommends video(s) for a scheduled topic.
 * <p>
 * <b>To plug in a real YouTube Data API v3 key:</b> set {@code youtube.api.key}
 * (application.properties or {@code YOUTUBE_API_KEY} env var). Get a key from
 * <a href="https://console.cloud.google.com/apis/credentials">Google Cloud Console</a>
 * (enable "YouTube Data API v3"). Free tier: 10,000 quota units/day —
 * {@link #getTopVideos} costs ~101 units per call (100 for search.list + 1 for
 * videos.list, since video IDs are batched into a single statistics lookup).
 * <p>
 * Without a key, falls back to a YouTube search-results URL — plan generation
 * never breaks just because content lookup did.
 */
@Component
public class ContentRecommender {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.api.key:}")
    private String youtubeApiKey;

    // How many raw search candidates to pull before filtering + ranking.
    // Kept wide because the oneshot/duration filter can eliminate a good chunk.
    private static final int CANDIDATE_POOL_SIZE = 50;

    // Titles containing any of these (case-insensitive) are excluded outright —
    // these are cram/marathon formats, not concept-building material.
    private static final List<String> EXCLUDE_KEYWORDS = List.of(
            "one shot", "oneshot", "1 shot", "marathon", "crash course",
            "in one video", "complete syllabus", "full syllabus", "last minute",
            "rapid revision", "24 hour", "24 hours", "all chapters", "full course in"
    );

    // Titles containing any of these get a scoring boost — signals of
    // structured, foundational, single-topic teaching.
    private static final List<String> CONCEPT_KEYWORDS = List.of(
            "concept", "detailed", "full chapter", "lecture", "class 11", "class 12",
            "ncert", "fundamentals", "basics", "in depth", "explained", "theory"
    );

    // Videos shorter than this are likely shorts/trailers/promos, not lectures.
    private static final long MIN_DURATION_SECONDS = 180; // 3 min
    // Videos longer than this are almost always oneshot/marathon-style cram
    // sessions covering an entire subject/chapter superficially, not one topic.
    private static final long MAX_DURATION_SECONDS = 7200; // 2 hours

    public boolean isConfigured() {
        return youtubeApiKey != null && !youtubeApiKey.isBlank();
    }

    /** A single ranked video result, with the raw engagement stats exposed
     *  so the frontend can display them (view count, likes, etc.) alongside
     *  the recommendation if desired. */
    public record RankedVideo(
            String videoId,
            String title,
            String channelTitle,
            String url,
            String thumbnailUrl,
            long viewCount,
            long likeCount,
            long commentCount,
            long durationSeconds,
            double score
    ) {
        public ContentOutput toContentOutput() {
            return new ContentOutput(title, url, thumbnailUrl, channelTitle, durationSeconds);
        }
    }

    // =====================================================================
    // Existing single-recommendation API (unchanged behavior/signature)
    // =====================================================================

    public ContentOutput recommend(ContentInput input) {
        List<RankedVideo> top = getTopVideos(input, 1);
        if (!top.isEmpty()) {
            return top.get(0).toContentOutput();
        }
        return placeholder(input);
    }

    public List<ContentOutput> recommendList(ContentInput input) {
        List<RankedVideo> top = getTopVideos(input, 10);
        if (!top.isEmpty()) {
            return top.stream().map(RankedVideo::toContentOutput).toList();
        }
        return List.of(placeholder(input));
    }

    // =====================================================================
    // NEW: top-N videos ranked by engagement, filtered for conceptual content
    // =====================================================================

    /**
     * Returns up to {@code topN} videos for this topic, ranked by a composite
     * engagement score (views, like-rate, comment-rate), after excluding
     * oneshot/marathon/cram-style content and out-of-range durations.
     * Falls back to an empty list if unconfigured or the API call fails —
     * caller should fall back to {@link #placeholder} in that case.
     */
    public List<RankedVideo> getTopVideos(ContentInput input, int topN) {
        if (!isConfigured()) {
            return List.of();
        }
        try {
            List<Map<String, String>> candidates = searchCandidates(input);
            if (candidates.isEmpty()) return List.of();

            List<RankedVideo> withStats = fetchStatsAndBuild(candidates);
            List<RankedVideo> filtered = withStats.stream()
                    .filter(this::passesFilters)
                    .toList();

            List<RankedVideo> scored = scoreAndRank(filtered);

            if (scored.size() < 10) {
                // Pad the list with unfiltered items at the END, so they don't outrank good videos
                java.util.Set<String> existingIds = scored.stream().map(RankedVideo::videoId).collect(java.util.stream.Collectors.toSet());
                
                // Score the remaining videos but penalize them so they stay at the bottom, or just append them directly
                List<RankedVideo> remaining = withStats.stream()
                        .filter(v -> !existingIds.contains(v.videoId()))
                        .toList();
                        
                List<RankedVideo> remainingScored = scoreAndRank(remaining);
                
                List<RankedVideo> padded = new java.util.ArrayList<>(scored);
                padded.addAll(remainingScored);
                scored = padded;
            }

            return scored.stream().limit(topN).toList();
        } catch (Exception e) {
            System.err.println("YouTube ranked-video lookup failed: " + e.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> searchRaw(String query) {
        if (!isConfigured()) return Map.of();
        String urlStr = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet&type=video&maxResults=1"
                + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&key=" + youtubeApiKey;
        try {
            Map<String, Object> response = restTemplate.getForObject(URI.create(urlStr), Map.class);
            return response != null ? response : Map.of();
        } catch (Exception e) {
            System.err.println("YouTube searchRaw failed: " + e.getMessage());
            return Map.of();
        }
    }

    private String buildQuery(ContentInput input) {
        String subjectPart = input.subjectName() != null ? input.subjectName() + " " : "";
        return subjectPart + input.topicName() + " concept lecture -\"one shot\" -oneshot -marathon";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> searchCandidates(ContentInput input) {
        String query = buildQuery(input);
        String urlStr = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet&type=video&maxResults=" + CANDIDATE_POOL_SIZE
                + "&order=relevance"
                + "&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&key=" + youtubeApiKey;

        Map<String, Object> response = restTemplate.getForObject(URI.create(urlStr), Map.class);
        List<Map<String, String>> out = new ArrayList<>();
        if (response == null) return out;

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items == null) return out;

        for (Map<String, Object> item : items) {
            Map<String, Object> id = (Map<String, Object>) item.get("id");
            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
            if (id == null || snippet == null) continue;
            String videoId = (String) id.get("videoId");
            if (videoId == null) continue;

            String title = cleanHtml((String) snippet.get("title"));
            String channelTitle = (String) snippet.get("channelTitle");
            String thumbnailUrl = extractThumbnail(snippet);

            out.add(Map.of(
                    "videoId", videoId,
                    "title", title != null ? title : "",
                    "channelTitle", channelTitle != null ? channelTitle : "",
                    "thumbnailUrl", thumbnailUrl != null ? thumbnailUrl : ""
            ));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private String extractThumbnail(Map<String, Object> snippet) {
        Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
        if (thumbnails == null) return "";
        Map<String, Object> medium = (Map<String, Object>) thumbnails.get("medium");
        if (medium != null) return (String) medium.get("url");
        Map<String, Object> def = (Map<String, Object>) thumbnails.get("default");
        return def != null ? (String) def.get("url") : "";
    }

    /** Batches all candidate video IDs into a single videos.list call
     *  (cheap: 1 quota unit total, vs. calling it once per video). */
    @SuppressWarnings("unchecked")
    private List<RankedVideo> fetchStatsAndBuild(List<Map<String, String>> candidates) {
        String idList = String.join(",", candidates.stream().map(c -> c.get("videoId")).toList());
        String urlStr = "https://www.googleapis.com/youtube/v3/videos"
                + "?part=statistics,contentDetails"
                + "&id=" + idList
                + "&key=" + youtubeApiKey;

        Map<String, Object> response = restTemplate.getForObject(URI.create(urlStr), Map.class);
        List<RankedVideo> out = new ArrayList<>();
        if (response == null) return out;

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items == null) return out;

        // Index candidate metadata (title/channel/thumbnail) by videoId for lookup.
        Map<String, Map<String, String>> byId = new java.util.HashMap<>();
        for (Map<String, String> c : candidates) byId.put(c.get("videoId"), c);

        for (Map<String, Object> item : items) {
            String videoId = (String) item.get("id");
            Map<String, String> meta = byId.get(videoId);
            if (meta == null) continue;

            Map<String, Object> stats = (Map<String, Object>) item.get("statistics");
            Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");

            long views = parseLong(stats != null ? stats.get("viewCount") : null);
            long likes = parseLong(stats != null ? stats.get("likeCount") : null);
            long comments = parseLong(stats != null ? stats.get("commentCount") : null);
            long durationSec = parseDurationSeconds(contentDetails != null ? (String) contentDetails.get("duration") : null);

            out.add(new RankedVideo(
                    videoId,
                    meta.get("title"),
                    meta.get("channelTitle"),
                    "https://www.youtube.com/watch?v=" + videoId,
                    meta.get("thumbnailUrl"),
                    views, likes, comments, durationSec,
                    0.0 // score filled in later
            ));
        }
        return out;
    }

    private boolean passesFilters(RankedVideo v) {
        if (v.durationSeconds() < MIN_DURATION_SECONDS || v.durationSeconds() > MAX_DURATION_SECONDS) {
            return false;
        }
        String lowerTitle = v.title().toLowerCase(Locale.ROOT);
        for (String bad : EXCLUDE_KEYWORDS) {
            if (lowerTitle.contains(bad)) return false;
        }
        return true;
    }

    /**
     * Composite engagement score:
     *   45% normalized log-scaled view count   (popularity, but log-damped so
     *                                            mega-channels don't totally
     *                                            drown out smaller good ones)
     *   35% normalized like-rate  (likes / views) — quality signal independent
     *                                                of channel size
     *   20% normalized comment-rate (comments / views) — engagement/discussion signal
     * Plus a 1.15x multiplier if the title contains a conceptual-learning
     * keyword (see CONCEPT_KEYWORDS), to bias toward foundational content.
     * All normalization is min-max within the current candidate pool, since
     * absolute view counts vary wildly by channel size/topic popularity.
     */
    private List<RankedVideo> scoreAndRank(List<RankedVideo> videos) {
        double maxLogViews = videos.stream()
                .mapToDouble(v -> Math.log10(v.viewCount() + 1)).max().orElse(1.0);
        double maxLikeRate = videos.stream()
                .mapToDouble(v -> v.likeCount() / (double) Math.max(v.viewCount(), 1)).max().orElse(1.0);
        double maxCommentRate = videos.stream()
                .mapToDouble(v -> v.commentCount() / (double) Math.max(v.viewCount(), 1)).max().orElse(1.0);

        List<RankedVideo> rescored = new ArrayList<>();
        for (RankedVideo v : videos) {
            double logViews = Math.log10(v.viewCount() + 1);
            double likeRate = v.likeCount() / (double) Math.max(v.viewCount(), 1);
            double commentRate = v.commentCount() / (double) Math.max(v.viewCount(), 1);

            double normViews = maxLogViews > 0 ? logViews / maxLogViews : 0;
            double normLikes = maxLikeRate > 0 ? likeRate / maxLikeRate : 0;
            double normComments = maxCommentRate > 0 ? commentRate / maxCommentRate : 0;

            double base = 0.45 * normViews + 0.35 * normLikes + 0.20 * normComments;

            String lowerTitle = v.title().toLowerCase(Locale.ROOT);
            boolean conceptual = CONCEPT_KEYWORDS.stream().anyMatch(lowerTitle::contains);
            double finalScore = conceptual ? base * 1.15 : base;

            rescored.add(new RankedVideo(
                    v.videoId(), v.title(), v.channelTitle(), v.url(), v.thumbnailUrl(),
                    v.viewCount(), v.likeCount(), v.commentCount(), v.durationSeconds(),
                    finalScore
            ));
        }
        rescored.sort(Comparator.comparingDouble(RankedVideo::score).reversed());
        return rescored;
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private long parseLong(Object val) {
        if (val == null) return 0L;
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /** ISO-8601 duration (e.g. "PT1H2M10S") -> total seconds. */
    private long parseDurationSeconds(String iso8601) {
        if (iso8601 == null || iso8601.isBlank()) return 0L;
        try {
            return Duration.parse(iso8601).getSeconds();
        } catch (Exception e) {
            return 0L;
        }
    }

    private String cleanHtml(String s) {
        if (s == null) return null;
        return s.replace("&quot;", "\"").replace("&#39;", "'").replace("&amp;", "&");
    }

    public ContentOutput placeholder(ContentInput input) {
        String query = buildQuery(input);
        String searchUrl = "https://www.youtube.com/results?search_query="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
        return new ContentOutput(input.topicName() + " — Concept Walkthrough", searchUrl, "", "", 0L);
    }
}
