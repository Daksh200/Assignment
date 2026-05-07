package com.assignment.gatewayguardrails.redis;

public final class RedisKeyBuilder {

    private RedisKeyBuilder() {
    }

    public static String postViralityScore(long postId) {
        return "post:" + postId + ":virality_score";
    }

    public static String postBotCount(long postId) {
        return "post:" + postId + ":bot_count";
    }

    public static String cooldownBotHuman(long botId, long humanId) {
        return "cooldown:bot_" + botId + ":human_" + humanId;
    }

    public static String userPendingNotifs(long userId) {
        return "user:" + userId + ":pending_notifs";
    }

    public static String userNotifCooldown(long userId) {
        return "user:" + userId + ":notif_cooldown";
    }

    public static String userWithPendingNotifsSet() {
        return "user_with_pending_notifs";
    }
}

