package com.younggeun.tabling.persist.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.younggeun.tabling.model.constants.ReservationStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "reservation")
public class ReservationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "phoneNumber")
    private UserEntity user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "store_id")
    private StoreEntity store;

    private LocalDate date;
    private LocalTime time;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.WAIT;

}
