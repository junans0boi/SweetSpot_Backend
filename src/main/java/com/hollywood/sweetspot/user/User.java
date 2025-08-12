// src/main/java/com/hollywood/sweetspot/user/User.java
package com.hollywood.sweetspot.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
// @Entity : 이 클래스가 DB 테이블과 매핑된다는 뜻.
// src/main/java/com/hollywood/sweetspot/user/User.java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true) // 소셜 계정은 null 허용
    private String password;

    @Column(nullable = false)
    private String roles; // e.g. "USER"

    // 🔽 추가 필드
    @Column(length = 20)
    private String provider; // GOOGLE, NAVER 등

    @Column(length = 100)
    private String providerId; // Google sub 값

    @Column(length = 100)
    private String name; // 표시 이름

    @Column(length = 300)
    private String pictureUrl; // 프로필 사진 URL
}