package com.younggeun.tabling.persist.dto;

import com.younggeun.tabling.model.constants.ReservationStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ReservationDto {
    private Long reservationId;
    private Long storeId;
    private LocalDate date;
    private LocalTime time;
    private ReservationStatus status;

}
