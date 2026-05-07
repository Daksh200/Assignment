package com.assignment.gatewayguardrails.repo;

import com.assignment.gatewayguardrails.domain.Bot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotRepository extends JpaRepository<Bot, Long> {
    Optional<Bot> findByName(String name);
}

