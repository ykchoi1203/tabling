package com.younggeun.tabling.persist;

import com.younggeun.tabling.persist.entity.ReservationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    @Query(value = "SELECT R.* " +
            "FROM RESERVATION AS R " +
            "JOIN STORE AS S ON R.store_id = S.id " +
            "JOIN PARTNER AS P ON S.user_id = P.id " +
            "WHERE P.partner_id = :partnerId AND R.date = :date " +
            "ORDER BY R.time",
            nativeQuery = true)
    Page<ReservationEntity> findAllByDateAndPartnerIdOrderByTime(
            Pageable pageable,
            @Param("partnerId") String partnerId,
            @Param("date") String date
    );

}
