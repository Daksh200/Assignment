package com.assignment.gatewayguardrails.service;

import com.assignment.gatewayguardrails.api.dto.CommentResponse;
import com.assignment.gatewayguardrails.api.dto.CreateCommentRequest;
import com.assignment.gatewayguardrails.api.dto.CreateLikeRequest;
import com.assignment.gatewayguardrails.api.dto.CreatePostRequest;
import com.assignment.gatewayguardrails.api.dto.PostResponse;
import com.assignment.gatewayguardrails.domain.Comment;
import com.assignment.gatewayguardrails.domain.Post;
import com.assignment.gatewayguardrails.domain.User;
import com.assignment.gatewayguardrails.redis.NotificationThrottlerService;
import com.assignment.gatewayguardrails.redis.RedisGuardrailsService;
import com.assignment.gatewayguardrails.repo.CommentRepository;
import com.assignment.gatewayguardrails.repo.PostRepository;
import com.assignment.gatewayguardrails.repo.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private final RedisGuardrailsService redisGuardrailsService;
    private final NotificationThrottlerService notificationThrottlerService;

    public PostService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            RedisGuardrailsService redisGuardrailsService,
            NotificationThrottlerService notificationThrottlerService
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.redisGuardrailsService = redisGuardrailsService;
        this.notificationThrottlerService = notificationThrottlerService;
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest req) {
        Post post = Post.builder()
                .authorType(req.authorType())
                .authorId(req.authorId())
                .content(req.content())
                .createdAt(Instant.now())
                .build();

        Post saved = postRepository.save(post);
        return toPostResponse(saved);
    }

    @Transactional
    public CommentResponse addComment(long postId, CreateCommentRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

        // Phase 2 vertical cap
        if (req.depthLevel() > 20) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Comment depth cap exceeded");
        }

        boolean authorIsBot = "BOT".equalsIgnoreCase(req.authorType());
        boolean authorIsHuman = "USER".equalsIgnoreCase(req.authorType());

        if (!authorIsBot && !authorIsHuman) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "authorType must be USER or BOT");
        }

        // Determine botId/humanId for cooldown and notification logic.
        long botId;
        long humanId;
        int viralityDelta;

        if (authorIsBot) {
            botId = req.authorId();
            // For a BOT comment: the cooldown is between the bot author and the human who owns the post.
            // We don't have a post owner in schema, so we use the comment target post's author as the human.
            // If the post author is a bot, we treat the post author as the humanId per test harness.
            humanId = resolveHumanIdForCooldown(post);
            viralityDelta = 1; // Bot reply
        } else {
            botId = resolveBotIdForCooldown(post);
            humanId = req.authorId();
            viralityDelta = 50; // Human comment
        }

        // Atomic locks: cooldown + horizontal cap
        RedisGuardrailsService.GuardrailsResult guardrails = redisGuardrailsService.tryBotInteraction(
                postId,
                botId,
                humanId,
                100,
                java.time.Duration.ofMinutes(10)
        );

        if (!guardrails.accepted()) {
            if (guardrails.cooldownBlocked()) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Cooldown cap exceeded");
            }
            if (guardrails.botCapExceeded()) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Horizontal bot cap exceeded");
            }
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Guardrail rejected interaction");
        }

        // Persist after Redis guardrails allow it.
        Comment comment = Comment.builder()
                .post(post)
                .authorType(req.authorType())
                .authorId(req.authorId())
                .content(req.content())
                .depthLevel(req.depthLevel())
                .createdAt(Instant.now())
                .build();

        Comment saved = commentRepository.save(comment);

        // Virality update
        redisGuardrailsService.incrementViralityScore(postId, viralityDelta);

        // Notification batching: only for bot interactions as per spec.
        if (authorIsBot) {
            User postOwnerHuman = userRepository.findById(humanId)
                    .orElse(User.builder().id(humanId).username("user-" + humanId).isPremium(false).build());

            String msg = "Bot " + botId + " replied to your post";
            notificationThrottlerService.handleBotInteractionNotification(
                    postOwnerHuman.getId(),
                    postOwnerHuman.getUsername(),
                    msg
            );
        }

        return toCommentResponse(saved);
    }

    @Transactional
    public CommentResponse like(long postId, CreateLikeRequest req) {
        // Treat "like" as a Human Like interaction (Phase 2 scoring rules).
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

        // Like is a human interaction for scoring; do not enforce bot-horizontal cap.
        redisGuardrailsService.incrementViralityScore(postId, 20);

        // Also model it as a comment-like entry at depth 0 for persistence.
        Comment comment = Comment.builder()
                .post(post)
                .authorType(req.authorType())
                .authorId(req.authorId())
                .content("[LIKE]")
                .depthLevel(0)
                .createdAt(Instant.now())
                .build();

        Comment saved = commentRepository.save(comment);
        return toCommentResponse(saved);
    }

    private PostResponse toPostResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthorId(),
                post.getAuthorType(),
                post.getContent(),
                post.getCreatedAt()
        );
    }

    private CommentResponse toCommentResponse(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getPost().getId(),
                c.getAuthorId(),
                c.getAuthorType(),
                c.getContent(),
                c.getDepthLevel(),
                c.getCreatedAt()
        );
    }

    private long resolveHumanIdForCooldown(Post post) {
        // If post author is human: use it. Otherwise use it as well (best-effort with current schema).
        return post.getAuthorId();
    }

    private long resolveBotIdForCooldown(Post post) {
        // If post author is a bot: use it. Otherwise use 0 as placeholder; guardrails will enforce cooldown/horizontal caps.
        return post.getAuthorId();
    }
}

