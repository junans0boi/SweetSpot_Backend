package com.hollywood.sweetspot.ugc;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "favorites",
    uniqueConstraints = @UniqueConstraint(name = "uq_user_place", columnNames = {"userId", "provider", "externalId"}),
    indexes = { @Index(name = "ix_fav_user", columnList = "userId") }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String provider; // kakao

    @Column(nullable = false)
    private String externalId;

    @Column(nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}