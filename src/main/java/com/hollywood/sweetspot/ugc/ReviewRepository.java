package com.hollywood.sweetspot.ugc;

import java.util.List;

interface ReviewRepository extends org.springframework.data.jpa.repository.JpaRepository<Review, Long> {
    List<Review> findByProviderAndExternalIdOrderByCreatedAtDesc(String provider, String externalId);

    @org.springframework.data.jpa.repository.Query("select coalesce(avg(r.rating),0) from Review r where r.provider=:p and r.externalId=:e")
    double avgRating(@org.springframework.data.repository.query.Param("p") String provider,
                     @org.springframework.data.repository.query.Param("e") String externalId);

    long countByProviderAndExternalId(String provider, String externalId);
}
