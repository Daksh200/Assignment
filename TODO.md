# TODO - Backend Engineering Assignment (Core API & Guardrails)

- [ ] Confirm compilation gaps by scanning imports/references (e.g., missing PostService).
- [ ] Implement Phase 1 persistence + REST behavior via `PostService`.
- [ ] Implement Phase 2 Redis virality increment + atomic guardrails (horizontal cap, vertical depth cap, cooldown TTL) using Lua/Redis atomic operations.
- [ ] Implement bot/human interaction scoring for post replies, comments, and likes.
- [ ] Enforce 429 Too Many Requests on horizontal cap exceeded.
- [ ] Implement Phase 3 notification engine: push into Redis list or log+set cooldown; add `@Scheduled` sweeper every 5 minutes to summarize and clear pending messages.
- [ ] Ensure statelessness: all counters/cooldowns/pending notifs stored in Redis.
- [ ] Add/verify DTO/request/response validation and mapping.
- [ ] Update README with approach + explanation of thread safety for atomic locks.
- [ ] Ensure Postman collection URLs match implemented endpoints.
- [ ] Add corner-case handling: depth cap >20 and race condition for 200 concurrent bot comments hitting exactly 100 cap.

