package com.younggeun.tabling.controller;

import com.younggeun.tabling.model.Auth;
import com.younggeun.tabling.persist.dto.ReservationDto;
import com.younggeun.tabling.persist.dto.StoreDto;
import com.younggeun.tabling.persist.entity.ReservationEntity;
import com.younggeun.tabling.persist.entity.StoreEntity;
import com.younggeun.tabling.security.TokenProvider;
import com.younggeun.tabling.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final TokenProvider tokenProvider;

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        var result = this.userService.register(request);
        return ResponseEntity.ok(result);
    }

    // 로그인
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        var user = this.userService.authenticate(request);
        var token = this.tokenProvider.generateToken(user.getUsername(), user.getRoles());

        return ResponseEntity.ok(token);
    }

    // 상점 목록
    @GetMapping("/store")
    public ResponseEntity<?> searchStore( @RequestParam(value = "lat", required = false, defaultValue = "126.73408") double lat,
                                            @RequestParam(value = "lon", required = false, defaultValue = "127.26931") double lon,
                                            @RequestParam(value = "orderBy", required = false, defaultValue = "거리순") String orderBy,
                                            final Pageable pageable) {
        Page<StoreDto> stores = this.userService.getAllStores(pageable, orderBy, lat, lon);
        return ResponseEntity.ok(stores);
    }

    // 상점 상세 목록
    @GetMapping("/store/detail")
    public ResponseEntity<?> searchStoreDetail(@RequestParam(value = "storeId") Long storeId) {
        StoreEntity store = this.userService.getStoreDetail(storeId);
        return ResponseEntity.ok(store);
    }

    // 상점 예약
    @PostMapping("/store/reservation")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> reservationStore(@RequestBody ReservationDto reservationDto,
                                              Authentication authentication) {
        ReservationEntity reservation = this.userService.reservation(authentication, reservationDto);
        return ResponseEntity.ok(reservation);
    }

}
