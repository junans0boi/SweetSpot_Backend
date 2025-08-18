package com.hollywood.sweetspot.ugc;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "reviews",
       indexes = {
           @Index(name="ix_review_place", columnList="provider,externalId"),
           @Index(name="ix_review_user", columnList="userId")
       })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용
@AllArgsConstructor
@Builder
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)     private Long userId;
    @Column(nullable=false)     private String provider;
    @Column(nullable=false)     private String externalId;
    @Column(nullable=false)     private int rating; // 1~5
    @Column(columnDefinition="TEXT") private String content;
    @Column(columnDefinition="TEXT") private String photosJson;

    @Column(nullable=false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}