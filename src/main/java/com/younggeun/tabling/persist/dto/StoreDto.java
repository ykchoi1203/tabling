package com.younggeun.tabling.persist.dto;

import com.younggeun.tabling.persist.entity.PartnerEntity;
import com.younggeun.tabling.persist.entity.ReservationEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class StoreDto {
    private String distanceDifference;
    private Long storeId;
    private String storeName;
    private Double latitude;
    private Double longitude;
    private Long totalStarRating;
    private Long totalReview;
    private PartnerEntity partner;
    private List<ReservationEntity> reservations = new ArrayList<>();
}
