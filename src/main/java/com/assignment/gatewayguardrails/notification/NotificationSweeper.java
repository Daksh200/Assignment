package com.assignment.gatewayguardrails.notification;

import com.assignment.gatewayguardrails.redis.NotificationThrottlerService;
import com.assignment.gatewayguardrails.redis.RedisKeyBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NotificationSweeper {

    private final NotificationThrottlerService notificationThrottlerService;
    private final StringRedisTemplate redis;

    public NotificationSweeper(NotificationThrottlerService notificationThrottlerService, StringRedisTemplate redis) {
        this.notificationThrottlerService = notificationThrottlerService;
        this.redis = redis;
    }

    // Runs every 5 minutes (testing purposes)
    @Scheduled(fixedDelayString = "${app.notification.cronSeconds:300}000")
    public void sweep() {
        Set<String> usersWithPending = redis.opsForSet().members(RedisKeyBuilder.userWithPendingNotifsSet());
        if (usersWithPending == null || usersWithPending.isEmpty()) {
            return;
        }

        for (String userIdStr : usersWithPending) {
            long userId = Long.parseLong(userIdStr);
            Long count = notificationThrottlerService.popAllPendingCount(userId);
            if (count == null || count == 0) {
                notificationThrottlerService.clearUserPendingListAndSet(userId);
                continue;
            }

            // Spec requires a summarized message; we log the count of pending interactions.
            System.out.println(
                    "Summarized Push Notification: Bot X and [" + count + "] others interacted with your posts."
            );
            notificationThrottlerService.clearUserPendingListAndSet(userId);
        }
    }
}


