# DIKSHA CALAS Java Backend — Final Improvement Notes

## Implemented in this build

- Adaptive topic sequencing using historical student study completion.
- Weak topics receive a controlled priority boost while TIS remains important.
- Weak topics receive additional study time (up to 35% extra below 50% mastery).
- Topic mastery is recency-weighted and derived from actual `DailyProgress` logs.
- Generated schedule notes expose the mastery signal used by the planner.
- YouTube API results take precedence over seeded placeholder resources when `YOUTUBE_API_KEY` is configured.
- Practice links no longer point to a non-existent `dikshacalas.edu` domain; they resolve to a topic-specific practice search until a first-party question bank is connected.
- Request validation added for auth and study-plan APIs.
- Study-plan date range and daily-hours validation added.
- Security logging reduced from DEBUG to INFO.
- Added `application-example.properties` for safe environment-based configuration.
- Added unit tests for topic sequencing, adaptive priority, macro phases, assessment cadence, and parallel micro planning.

## Important limitation

The application currently stores study completion/hours, but it does not yet store per-question test attempts and accuracy. Therefore the adaptive mastery score is a **study-completion proxy**, not a full exam-performance model. A future question-bank/test-attempt module can feed accuracy into `TopicMasteryService` without changing the planner architecture.
