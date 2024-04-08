package com.younggeun.tabling.controller;

import com.younggeun.tabling.model.Auth;
import com.younggeun.tabling.persist.dto.ReservationDto;
import com.younggeun.tabling.persist.dto.StoreDto;
import com.younggeun.tabling.persist.entity.ReservationEntity;
import com.younggeun.tabling.security.TokenProvider;
import com.younggeun.tabling.service.PartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/partner")
@RequiredArgsConstructor
public class PartnerController {
    private final PartnerService partnerService;
    private final TokenProvider tokenProvider;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        var result = this.partnerService.register(request);
        return ResponseEntity.ok(result);
    }

    // 로그인
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        var user = this.partnerService.authenticate(request);
        var token = this.tokenProvider.generateToken(user.getUsername(), user.getRoles());
        return ResponseEntity.ok(token);
    }

    // store 등록
    @PostMapping("/store/register")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> registerStore(@RequestBody StoreDto storeDto, Authentication authentication) {
        var result = this.partnerService.registerStore(authentication, storeDto);
        return ResponseEntity.ok(result);
    }

    // store update
    @PutMapping("/store/update")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> updateStore(@RequestBody StoreDto storeDto, Authentication authentication) {
        var result = this.partnerService.updateStore(authentication, storeDto);
        return ResponseEntity.ok(result);
    }

    // store 삭제
    @DeleteMapping("/store/delete")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> deleteStore(@RequestBody StoreDto storeDto, Authentication authentication) {
        var result = this.partnerService.deleteStore(authentication, storeDto.getStoreId());
        return ResponseEntity.ok(result);
    }

    // 예약 정보 확인(날짜별 시간 테이블 목록)
    @GetMapping("store/reservation")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> searchReservation(@RequestParam(value = "date", required = false) String date,
                                          final Pageable pageable,
                                          Authentication authentication) {
        if (date == null) {
            date = LocalDate.now().toString();
        }
        System.out.println(date);

        Page<ReservationEntity> reservation = this.partnerService.getReservation(pageable, date, authentication);
        return ResponseEntity.ok(reservation);
    }

    // 예약 확정/취소
    @PutMapping("/store/reservation/allow")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> updateReservation(@RequestBody ReservationDto reservationDto, Authentication authentication) {
        var result = this.partnerService.updateReservation(authentication, reservationDto);
        return ResponseEntity.ok(result);
    }

    // 예약 도착
    @PutMapping("/store/reservation/arrival")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> arrivalReservation(@RequestBody ReservationDto reservationDto, Authentication authentication) {
        var result = this.partnerService.arrivalReservation(authentication, reservationDto);
        return ResponseEntity.ok(result);
    }

}
