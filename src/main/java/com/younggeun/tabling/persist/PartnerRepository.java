package com.younggeun.tabling.persist;

import com.younggeun.tabling.persist.entity.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerRepository extends JpaRepository<PartnerEntity, Long> {
    Optional<PartnerEntity> findByPartnerId(String partnerId);

    boolean existsByPartnerId(String userId);
}
