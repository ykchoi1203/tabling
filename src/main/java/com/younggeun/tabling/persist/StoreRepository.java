package com.younggeun.tabling.persist;

import com.younggeun.tabling.persist.dto.StoreDto;
import com.younggeun.tabling.persist.entity.StoreEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    Optional<StoreEntity> findByStoreName(String storeName);
    Optional<StoreEntity> findById(Long id);
    Page<StoreDto> findAllByOrderByStoreName(Pageable pageable);

    boolean existsByStoreName(String storeName);

    @Query(value = "SELECT ROUND(SQRT(POWER(111.045 * (CAST(store.latitude AS DECIMAL(10, 6)) - :lat), 2) + " +
            " POWER(111.045 * (:lon - CAST(store.longitude AS DECIMAL(10, 6))) * COS(CAST(store.latitude AS DECIMAL(10, 6)) / 57.3), 2)), 6) AS distance_difference, store.* " +
            " FROM store " +
            " ORDER BY distance_difference ",
            nativeQuery = true)
    Page<StoreDto> findAllByOrderByDistance(Pageable pageable, @Param("lat") double lat, @Param("lon") double lon);

    @Query(value = "SELECT ROUND(store.total_star_rating / store.total_review) AS star_rating, store.* " +
            " FROM store " +
            " ORDER BY star_rating ",
            nativeQuery = true)
    Page<StoreDto> findAllByOrderByStarRating(Pageable pageable);

    void deleteById(Long storeId);
}
