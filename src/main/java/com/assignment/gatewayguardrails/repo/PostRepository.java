package com.assignment.gatewayguardrails.repo;

import com.assignment.gatewayguardrails.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}

