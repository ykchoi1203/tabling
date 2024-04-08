package com.younggeun.tabling.controller;

import com.younggeun.tabling.persist.dto.ReviewDto;
import com.younggeun.tabling.service.ReviewService;
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
    @PostMapping("/write")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> review(@RequestBody ReviewDto reviewDto, Authentication authentication) {
        var result = this.reviewService.writeReview(authentication, reviewDto);
        return ResponseEntity.ok(result);
    }

    // review 수정
    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateReview(@RequestBody ReviewDto reviewDto, Authentication authentication) {
        var result = this.reviewService.updateReview(authentication, reviewDto);
        return ResponseEntity.ok(result);
    }

    // review 삭제
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
    @PostMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> selectReview(Authentication authentication, Pageable pageable) {
        var result = this.reviewService.selectReview(authentication, pageable);
        return ResponseEntity.ok(result);
    }

}
