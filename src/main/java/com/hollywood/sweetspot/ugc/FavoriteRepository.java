package com.hollywood.sweetspot.ugc;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends org.springframework.data.jpa.repository.JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndProviderAndExternalId(Long userId, String provider, String externalId);
    List<Favorite> findByUserId(Long userId);
    void deleteByUserIdAndProviderAndExternalId(Long userId, String provider, String externalId);
}