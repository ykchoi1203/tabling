package com.younggeun.tabling.persist.dto;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDto {
    private Long reviewId;
    private Long reservationId;
    private int starRating;
    private String reviewText;
}
