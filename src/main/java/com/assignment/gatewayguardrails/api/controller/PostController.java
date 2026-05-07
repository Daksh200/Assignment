package com.assignment.gatewayguardrails.api.controller;

import com.assignment.gatewayguardrails.api.dto.CommentResponse;
import com.assignment.gatewayguardrails.api.dto.CreateCommentRequest;
import com.assignment.gatewayguardrails.api.dto.CreateLikeRequest;
import com.assignment.gatewayguardrails.api.dto.CreatePostRequest;
import com.assignment.gatewayguardrails.api.dto.PostResponse;
import com.assignment.gatewayguardrails.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest req) {
        return postService.createPost(req);
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
            @PathVariable long postId,
            @Valid @RequestBody CreateCommentRequest req
    ) {
        return postService.addComment(postId, req);
    }

    @PostMapping("/posts/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse likePost(
            @PathVariable long postId,
            @Valid @RequestBody CreateLikeRequest req
    ) {
        return postService.like(postId, req);
    }
}


