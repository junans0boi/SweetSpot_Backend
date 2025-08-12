// src/main/java/com/hollywood/sweetspot/user/UserRepository.java
package com.hollywood.sweetspot.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// extends JpaRepository<User, Long> : 제네릭(타입 파라미터) → 엔티티 타입 User, PK 타입 Long.
public interface UserRepository extends JpaRepository<User, Long> {
    //  메서드 이름만으로 쿼리를 만들어주는 스프링 데이터 JPA의 “쿼리 메서드”.
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}