package com.assignment.gatewayguardrails.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisGuardrailsService {

    private final StringRedisTemplate redis;

    public RedisGuardrailsService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // Lua script to enforce cooldown + horizontal bot cap atomically.
    // KEYS:
    // 1 cooldown key
    // 2 bot_count key
    // ARGV:
    // 1 maxBotCount (e.g. 100)
    // 2 cooldownSeconds (e.g. 600)
    // Returns:
    // 0 => cooldown exists (reject)
    // 1 => accepted (cooldown set, bot_count incremented)
    // 2 => bot cap exceeded (reject)
    private static final String GUARDRAILS_LUA = """
            local cooldownKey = KEYS[1]
            local botCountKey = KEYS[2]
            local maxBotCount = tonumber(ARGV[1])
            local cooldownSeconds = tonumber(ARGV[2])

            if redis.call('EXISTS', cooldownKey) == 1 then
              return 0
            end

            local newCount = redis.call('INCR', botCountKey)
            if newCount > maxBotCount then
              return 2
            end

            redis.call('SET', cooldownKey, '1', 'EX', cooldownSeconds)
            return 1
            """;

    private final RedisScript<Long> guardrailsScript = RedisScript.of(GUARDRAILS_LUA, Long.class);

    public GuardrailsResult tryBotInteraction(long postId, long botId, long humanId, int maxBotCount, Duration cooldown) {
        String cooldownKey = RedisKeyBuilder.cooldownBotHuman(botId, humanId);
        String botCountKey = RedisKeyBuilder.postBotCount(postId);

        Long code = redis.execute(
                guardrailsScript,
                List.of(cooldownKey, botCountKey),
                String.valueOf(maxBotCount),
                String.valueOf(cooldown.getSeconds())
        );

        if (code == null) {
            return GuardrailsResult.accepted();
        }
        return switch (code.intValue()) {
            case 0 -> GuardrailsResult.cooldownBlocked();
            case 2 -> GuardrailsResult.botCapExceeded();
            default -> GuardrailsResult.accepted();
        };
    }

    public void incrementViralityScore(long postId, int delta) {
        redis.opsForValue().increment(RedisKeyBuilder.postViralityScore(postId), delta);
    }

    public record GuardrailsResult(boolean accepted, boolean cooldownBlocked, boolean botCapExceeded) {
        public static GuardrailsResult accepted() {
            return new GuardrailsResult(true, false, false);
        }

        public static GuardrailsResult cooldownBlocked() {
            return new GuardrailsResult(false, true, false);
        }

        public static GuardrailsResult botCapExceeded() {
            return new GuardrailsResult(false, false, true);
        }
    }

}

