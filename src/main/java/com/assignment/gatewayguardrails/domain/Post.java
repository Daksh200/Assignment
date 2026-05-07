package com.assignment.gatewayguardrails.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // polymorphic author reference (User or Bot)
    @Column(name = "author_type", nullable = false, length = 10)
    private String authorType; // "USER" | "BOT"

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

