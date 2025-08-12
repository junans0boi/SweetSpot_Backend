package com.hollywood.sweetspot.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
// efreshToken 엔티티를 다루는 Repository이며, 기본키 타입이 Long
// CRUD 메서드(findAll, save, deleteById 등)를 자동으로 제공.
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // findBy : SELECT 쿼리를 실행
    // TokenHash : 엔티티의 tokenHash 필드를 조건으로 사용,
    // AndRevokedFalse : revoked 필드가 false인 경우만
    Optional<RefreshToken> findByTokenIdAndRevokedFalse(String tokenId);
    // deleteBy : DELETE 쿼리를 실행
	// UserId : userId 필드가 매개변수와 일치하는 행 삭제
    void deleteByUserId(Long userId);
    // deleteBy : DELETE 쿼리
	// ExpiresAtBefore : expiresAt 필드가 매개변수(before)보다 이전인 경우
    long deleteByExpiresAtBefore(Instant before);
}