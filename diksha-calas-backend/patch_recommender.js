const fs = require('fs');
let content = fs.readFileSync('src/main/java/com/diksha/service/engine/ContentRecommender.java', 'utf8');

const oldLogic = `            if (filtered.size() < 8) {
                // Pad the list with unfiltered items to ensure we return enough videos
                java.util.Set<String> existingIds = filtered.stream().map(RankedVideo::videoId).collect(java.util.stream.Collectors.toSet());
                List<RankedVideo> padded = new java.util.ArrayList<>(filtered);
                for (RankedVideo v : withStats) {
                    if (!existingIds.contains(v.videoId())) {
                        padded.add(v);
                    }
                }
                filtered = padded;
            }

            List<RankedVideo> scored = scoreAndRank(filtered);
            return scored.stream().limit(topN).toList();`;

const newLogic = `            List<RankedVideo> scored = scoreAndRank(filtered);

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

            return scored.stream().limit(topN).toList();`;

content = content.replace(oldLogic, newLogic);
fs.writeFileSync('src/main/java/com/diksha/service/engine/ContentRecommender.java', content);
