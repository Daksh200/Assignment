package com.assignment.gatewayguardrails.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class NotificationThrottlerService {

    private final StringRedisTemplate redis;

    private final Duration notifCooldown;
    private final Duration pendingListTtl; // optional; we keep list until sweeper clears

    public NotificationThrottlerService(
            StringRedisTemplate redis,
            @Value("${app.notification.cooldownMinutes:15}") long cooldownMinutes
    ) {
        this.redis = redis;
        this.notifCooldown = Duration.ofMinutes(cooldownMinutes);
        this.pendingListTtl = Duration.ofMinutes(cooldownMinutes);
    }

    // If cooldown exists: push message into list.
    // Else: log immediate send + set cooldown.
    public void handleBotInteractionNotification(long userId, String botUsernameOrName, String message) {
        String cooldownKey = RedisKeyBuilder.userNotifCooldown(userId);
        String pendingListKey = RedisKeyBuilder.userPendingNotifs(userId);

        Boolean cooldownExists = redis.hasKey(cooldownKey);
        if (Boolean.TRUE.equals(cooldownExists)) {
            redis.opsForList().rightPush(pendingListKey, message);
            redis.opsForSet().add(RedisKeyBuilder.userWithPendingNotifsSet(), String.valueOf(userId));
            return;
        }

        System.out.println("Push Notification Sent to User: " + botUsernameOrName);
        redis.opsForValue().set(cooldownKey, "1", notifCooldown);
    }

    public long enqueuePendingIfCooldown(long userId, String message) {
        String pendingListKey = RedisKeyBuilder.userPendingNotifs(userId);
        redis.opsForList().rightPush(pendingListKey, message);
        redis.opsForSet().add(RedisKeyBuilder.userWithPendingNotifsSet(), String.valueOf(userId));
        return redis.opsForList().size(pendingListKey);
    }

    public Long popAllPendingCount(long userId) {
        String pendingListKey = RedisKeyBuilder.userPendingNotifs(userId);
        Long size = redis.opsForList().size(pendingListKey);
        if (size == null || size == 0) {
            return 0L;
        }
        // Pop all items.
        for (long i = 0; i < size; i++) {
            redis.opsForList().leftPop(pendingListKey);
        }
        return size;
    }

    public void clearUserPendingListAndSet(long userId) {
        redis.opsForList().trim(RedisKeyBuilder.userPendingNotifs(userId), 1, 0);
        redis.opsForSet().remove(RedisKeyBuilder.userWithPendingNotifsSet(), String.valueOf(userId));
    }
}

