package com.younggeun.tabling.service;

import com.younggeun.tabling.model.Auth;
import com.younggeun.tabling.model.constants.ReservationStatus;
import com.younggeun.tabling.persist.PartnerRepository;
import com.younggeun.tabling.persist.ReservationRepository;
import com.younggeun.tabling.persist.StoreRepository;
import com.younggeun.tabling.persist.dto.ReservationDto;
import com.younggeun.tabling.persist.dto.StoreDto;
import com.younggeun.tabling.persist.entity.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class PartnerService implements UserDetailsService {

    private PartnerRepository partnerRepository;
    private StoreRepository storeRepository;
    private ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return this.partnerRepository.findByPartnerId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다 -> " + userId));
    }

    // Partner 회원가입
    public PartnerEntity register(Auth.SignUp user) {
        if(this.partnerRepository.existsByPartnerId(user.getUserId())) {
            throw new RuntimeException("이미 사용중인 아이디 입니다.");
        }

        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        return this.partnerRepository.save(user.toPartnerEntity());
    }

    // Partner 로그인
    public PartnerEntity authenticate(Auth.SignIn user) {
        var member = this.partnerRepository.findByPartnerId(user.getUserId()).orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));

        if(!this.passwordEncoder.matches(user.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }


    // 상점 추가
    public StoreEntity registerStore(Authentication authentication, StoreDto storeDto) {
        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName()).orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));

        if(this.storeRepository.existsByStoreName(storeDto.getStoreName())) {
            throw new RuntimeException("이미 사용중인 상호명 입니다.");
        }

        return this.storeRepository.save(StoreEntity.builder()
                .storeName(storeDto.getStoreName())
                .latitude(storeDto.getLatitude())
                .longitude(storeDto.getLongitude())
                .partner(partner)
                .totalReview(0L)
                .totalStarRating(0L)
                .build());
    }

    // 상점 수정
    public StoreEntity updateStore(Authentication authentication, StoreDto storeDto) {
        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName()).orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));

        StoreEntity store = storeRepository.getById(storeDto.getStoreId());

        if(!Objects.equals(store.getPartner().getPartnerId(), partner.getPartnerId())) {
            throw new RuntimeException("로그인한 유저와 매장 관리자 유저가 다릅니다.");
        }

        store.setStoreName(storeDto.getStoreName());
        store.setLatitude(storeDto.getLatitude());
        store.setLongitude(storeDto.getLongitude());

        return this.storeRepository.save(store);
    }

    // 상점 삭제

    public boolean deleteStore(Authentication authentication, Long storeId) {
        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName()).orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));
        StoreEntity store = storeRepository.findById(storeId).orElseThrow(() -> new RuntimeException("해당 가게가 존재하지 않습니다."));
        if(store.getPartner().getPartnerId().equals(partner.getPartnerId())) {
            this.storeRepository.deleteById(storeId);
            return true;
        } else {
            throw new RuntimeException("로그인된 유저와 매장 관리자 유저가 다릅니다.");
        }
    }

    // 예약 확인
    public Page<ReservationEntity> getReservation(Pageable pageable, String date, Authentication authentication) {
        return reservationRepository.findAllByDateAndPartnerIdOrderByTime(pageable, authentication.getName(), date);
    }

    // 예약 승인/거절 (승인 : RESERVATION, 거절 : CANCEL )
    public ReservationEntity updateReservation(Authentication authentication, ReservationDto reservationDto) {
        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName()).orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));

        ReservationEntity reservation = reservationRepository.findById(reservationDto.getReservationId()).orElseThrow(()-> new RuntimeException("존재하지 않는 예약정보입니다."));

        StoreEntity store = storeRepository.findById(reservation.getStore().getId()).orElseThrow(() -> new RuntimeException("해당 가게가 존재하지 않습니다."));

        if(!Objects.equals(partner.getPartnerId(), store.getPartner().getPartnerId())) {
            throw new RuntimeException("해당 매장의 유저와 로그인된 유저가 다릅니다.");
        }

        if(!Objects.equals(reservation.getStore().getId(), store.getId())) {
            throw new RuntimeException("해당 매장의 예약 내역이 아닙니다.");
        }

        reservation.setStatus(reservationDto.getStatus());

        return this.reservationRepository.save(reservation);
    }

    // 매장 방문 후 도착 확인
    public ReservationEntity arrivalReservation(Authentication authentication, ReservationDto reservationDto) {
        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName())
                .orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));

        ReservationEntity reservation = reservationRepository.findById(reservationDto.getReservationId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 예약정보입니다."));

        StoreEntity store = reservation.getStore();

        if (!Objects.equals(partner.getPartnerId(), store.getPartner().getPartnerId())) {
            throw new RuntimeException("해당 매장의 유저와 로그인된 유저가 다릅니다.");
        }

        if (reservation.getStatus() == ReservationStatus.ARRIVAL) {
            throw new RuntimeException("이미 처리된 예약내역입니다.");
        }

        if (reservation.getStatus() != ReservationStatus.RESERVATION) {
            throw new RuntimeException("예약이 대기중이거나 취소되었습니다.");
        }

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        if (currentDate.getYear() != reservation.getDate().getYear()
                || currentDate.getMonthOfYear() != reservation.getDate().getMonthValue()
                || currentDate.getDayOfMonth() != reservation.getDate().getDayOfMonth()
                || currentTime.isAfter(reservation.getTime())
                || currentTime.isBefore(reservation.getTime().minusMinutes(10))
                || currentTime.isAfter(reservation.getTime().plusMinutes(10))) {
            throw new RuntimeException("예약 도착 완료할 수 없는 시간입니다.");
        }

        reservation.setStatus(ReservationStatus.ARRIVAL);
        return reservationRepository.save(reservation);
    }
}
