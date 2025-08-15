package com.hollywood.sweetspot.auth;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens",
       indexes = {@Index(name="ix_prt_email", columnList = "email"),
                  @Index(name="ix_prt_expires", columnList = "expiresAt")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String email;

    @Column(nullable=false, unique=true, length=36)
    private String tokenId;

    @Column(nullable=false, length=200)
    private String tokenHash;

    @Column(nullable=false)
    private Instant expiresAt;

    @Column(nullable=false)
    private boolean used;
}