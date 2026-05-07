package com.assignment.gatewayguardrails.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // polymorphic author reference (User or Bot)
    @Column(name = "author_type", nullable = false, length = 10)
    private String authorType; // "USER" | "BOT"

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "depth_level", nullable = false)
    private int depthLevel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

