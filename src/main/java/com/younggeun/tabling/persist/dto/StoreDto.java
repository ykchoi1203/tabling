package com.younggeun.tabling.persist.dto;

import lombok.*;

import javax.persistence.Column;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class StoreDto {
    private Long storeId;
    private String storeName;

    @Column(nullable = false)
    private Double latitude; // 위도
    @Column(nullable = false)
    private Double longitude; // 경도

}
