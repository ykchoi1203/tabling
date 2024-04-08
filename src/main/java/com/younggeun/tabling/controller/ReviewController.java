package com.younggeun.tabling.controller;

import com.younggeun.tabling.persist.dto.ReviewDto;
import com.younggeun.tabling.service.ReviewService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // review 등록
    @ApiOperation(value = "리뷰 등록", notes = "예약 상태가 ARRIVAL의 리뷰를 작성합니다. 다른 상태라면 등록 실패")
    @PostMapping("/write")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> review(@RequestBody ReviewDto reviewDto, Authentication authentication) {
        var result = this.reviewService.writeReview(authentication, reviewDto);
        return ResponseEntity.ok(result);
    }

    // review 수정
    @ApiOperation(value = "리뷰 수정", notes = "해당 작성자만 수정이 가능 합니다.")
    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateReview(@RequestBody ReviewDto reviewDto, Authentication authentication) {
        var result = this.reviewService.updateReview(authentication, reviewDto);
        return ResponseEntity.ok(result);
    }

    // review 삭제
    @ApiOperation(value = "리뷰 삭제", notes = "해당 작성자 혹은 해당 점주가 리뷰를 삭제합니다. 리뷰 삭제와 함께 Store의 별점도 같이 수정됩니다.")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('USER','PARTNER')")
    public ResponseEntity<?> deleteReview(@RequestBody ReviewDto reviewDto, Authentication authentication) {
        var authorities = authentication.getAuthorities();
        boolean result;

        if (authorities.contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            result = this.reviewService.deleteReviewByUser(authentication, reviewDto);
        } else if (authorities.contains(new SimpleGrantedAuthority("ROLE_PARTNER"))) {
            result = this.reviewService.deleteReviewByPartner(authentication, reviewDto);
        } else {
            throw new RuntimeException("Unknown role");
        }

        return ResponseEntity.ok(result);
    }

    // review 목록
    @ApiOperation(value = "리뷰 목록", notes = "해당 유저가 작성한 리뷰 목록을 보여줍니다.")
    @PostMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> selectReview(Authentication authentication, Pageable pageable) {
        var result = this.reviewService.selectReview(authentication, pageable);
        return ResponseEntity.ok(result);
    }

}
