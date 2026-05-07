package com.assignment.gatewayguardrails.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "persona_description", nullable = false, length = 2000)
    private String personaDescription;
}

