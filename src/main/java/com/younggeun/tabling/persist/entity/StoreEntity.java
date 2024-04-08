package com.younggeun.tabling.persist.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "store")
public class StoreEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName;

    @Column(nullable = false)
    private Double latitude; // 위도
    @Column(nullable = false)
    private Double longitude; // 경도

    private Long totalStarRating;
    private Long totalReview;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private PartnerEntity partner;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationEntity> reservations = new ArrayList<>();

}
