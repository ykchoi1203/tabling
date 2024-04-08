package com.younggeun.tabling.persist;

import com.younggeun.tabling.persist.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    Page<ReviewEntity> findByUserId(Long userId, Pageable pageable);

    @Query(value = "SELECT count(r) FROM review r WHERE r.reservation.Id = :reservationId")
    int countByReservationId(@Param("reservationId") Long reservationId);
}
