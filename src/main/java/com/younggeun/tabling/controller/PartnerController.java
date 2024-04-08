package com.younggeun.tabling.controller;

import com.younggeun.tabling.model.Auth;
import com.younggeun.tabling.persist.dto.ReservationDto;
import com.younggeun.tabling.persist.dto.StoreDto;
import com.younggeun.tabling.persist.entity.ReservationEntity;
import com.younggeun.tabling.security.TokenProvider;
import com.younggeun.tabling.service.PartnerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@Api(tags = "Partner Controller")
@Slf4j
@RestController
@RequestMapping("/partner")
@RequiredArgsConstructor
public class PartnerController {
    private final PartnerService partnerService;
    private final TokenProvider tokenProvider;

    // 회원 가입
    @ApiOperation(value = "Partner 회원가입", notes = "Partner 권한 부여와 함께 회원가입.")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        var result = this.partnerService.register(request);
        return ResponseEntity.ok(result);
    }

    // 로그인
    @ApiOperation(value = "Partner 로그인")
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        var user = this.partnerService.authenticate(request);
        var token = this.tokenProvider.generateToken(user.getUsername(), user.getRoles());
        return ResponseEntity.ok(token);
    }

    // store 등록
    @ApiOperation(value = "Store 등록")
    @PostMapping("/store/register")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> registerStore(@RequestBody StoreDto storeDto, Authentication authentication) {
        var result = this.partnerService.registerStore(authentication, storeDto);
        return ResponseEntity.ok(result);
    }

    // store update
    @ApiOperation(value = "Store 수정")
    @PutMapping("/store/update")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> updateStore(@RequestBody StoreDto storeDto, Authentication authentication) {
        var result = this.partnerService.updateStore(authentication, storeDto);
        return ResponseEntity.ok(result);
    }

    // store 삭제
    @ApiOperation(value = "Store 삭제")
    @DeleteMapping("/store/delete")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> deleteStore(@RequestBody StoreDto storeDto, Authentication authentication) {
        var result = this.partnerService.deleteStore(authentication, storeDto.getStoreId());
        return ResponseEntity.ok(result);
    }

    // 예약 정보 확인(날짜별 시간 테이블 목록)
    @ApiOperation(value = "예약 정보 확인", notes = "날짜로 조회하여 예약 정보를 확인합니다. 날짜를 입력 안할 시 현재 날짜가 기본값으로 들어갑니다.")
    @GetMapping("store/reservation")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> searchReservation(@RequestParam(value = "date", required = false) String date,
                                          final Pageable pageable,
                                          Authentication authentication) {
        if (date == null) {
            date = LocalDate.now().toString();
        }

        Page<ReservationEntity> reservation = this.partnerService.getReservation(pageable, date, authentication);
        return ResponseEntity.ok(reservation);
    }

    // 예약 확정/취소
    @ApiOperation(value = "예약 확정 / 취소", notes = "Status를 예약 확정 혹은 취소로 변경합니다. WAIT -> (RESERVATION / CANCEL)")
    @PutMapping("/store/reservation/allow")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> updateReservation(@RequestBody ReservationDto reservationDto, Authentication authentication) {
        var result = this.partnerService.updateReservation(authentication, reservationDto);
        return ResponseEntity.ok(result);
    }

    // 예약 도착
    @ApiOperation(value = "예약 도착 확인", notes = "예약 시간 10분 전부터 예약시간까지 입장 시 Status를 ARRIVAL 로 변경합니다.")
    @PutMapping("/store/reservation/arrival")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> arrivalReservation(@RequestBody ReservationDto reservationDto, Authentication authentication) {
        var result = this.partnerService.arrivalReservation(authentication, reservationDto);
        return ResponseEntity.ok(result);
    }

}
