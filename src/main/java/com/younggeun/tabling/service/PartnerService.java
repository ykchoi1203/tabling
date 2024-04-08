package com.younggeun.tabling.service;

import com.younggeun.tabling.exception.impl.*;
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
import org.springframework.transaction.annotation.Transactional;

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
            throw new AlreadyExistUserException();
        }

        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        return this.partnerRepository.save(user.toPartnerEntity());
    }

    // Partner 로그인
    public PartnerEntity authenticate(Auth.SignIn user) {
        var member = this.partnerRepository.findByPartnerId(user.getUserId()).orElseThrow(NoUserException::new);

        if(!this.passwordEncoder.matches(user.getPassword(), member.getPassword())) {
            throw new PasswordMismatchException();
        }
        return member;
    }


    // 상점 추가
    public StoreEntity registerStore(Authentication authentication, StoreDto storeDto) {
        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName()).orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));

        if(this.storeRepository.existsByStoreName(storeDto.getStoreName())) {
            throw new AlreadyExistStoreException();
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
            throw new MismatchPartnerException();
        }

        store.setStoreName(storeDto.getStoreName());
        store.setLatitude(storeDto.getLatitude());
        store.setLongitude(storeDto.getLongitude());

        return this.storeRepository.save(store);
    }

    // 상점 삭제
    @Transactional
    public boolean deleteStore(Authentication authentication, Long storeId) {
        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName()).orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));
        StoreEntity store = storeRepository.findById(storeId).orElseThrow(NoStoreException::new);
        if(store.getPartner().getPartnerId().equals(partner.getPartnerId())) {
            this.storeRepository.deleteById(storeId);
            return true;
        } else {
            throw new MismatchPartnerException();
        }
    }

    // 예약 확인
    public Page<ReservationEntity> getReservation(Pageable pageable, String date, Authentication authentication) {
        return reservationRepository.findAllByDateAndPartnerIdOrderByTime(pageable, authentication.getName(), date);
    }

    // 예약 승인/거절 (승인 : RESERVATION, 거절 : CANCEL )
    public ReservationEntity updateReservation(Authentication authentication, ReservationDto reservationDto) {
        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName()).orElseThrow(NoUserException::new);

        ReservationEntity reservation = reservationRepository.findById(reservationDto.getReservationId()).orElseThrow(NoReservationException::new);

        StoreEntity store = storeRepository.findById(reservation.getStore().getId()).orElseThrow(NoStoreException::new);

        if(!Objects.equals(partner.getPartnerId(), store.getPartner().getPartnerId())) {
            throw new MismatchPartnerException();
        }

        if(!Objects.equals(reservation.getStore().getId(), store.getId())) {
            throw new MismatchReservationException();
        }

        reservation.setStatus(reservationDto.getStatus());

        return this.reservationRepository.save(reservation);
    }

    // 매장 방문 후 도착 확인
    public ReservationEntity arrivalReservation(Authentication authentication, ReservationDto reservationDto) {
        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName())
                .orElseThrow(NoUserException::new);

        ReservationEntity reservation = reservationRepository.findById(reservationDto.getReservationId())
                .orElseThrow(NoReservationException::new);

        StoreEntity store = reservation.getStore();

        if (!Objects.equals(partner.getPartnerId(), store.getPartner().getPartnerId())) {
            throw new MismatchPartnerException();
        }

        if (reservation.getStatus() == ReservationStatus.ARRIVAL) {
            throw new AlreadyExistReservationException();
        }

        if (reservation.getStatus() != ReservationStatus.RESERVATION) {
            throw new WaitOrCancelReservationException();
        }

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        if (currentDate.getYear() != reservation.getDate().getYear()
                || currentDate.getMonthOfYear() != reservation.getDate().getMonthValue()
                || currentDate.getDayOfMonth() != reservation.getDate().getDayOfMonth()
                || currentTime.isAfter(reservation.getTime())
                || currentTime.isBefore(reservation.getTime().minusMinutes(10))
                || currentTime.isAfter(reservation.getTime().plusMinutes(10))) {
            throw new CannotReservationTimeException();
        }

        reservation.setStatus(ReservationStatus.ARRIVAL);
        return reservationRepository.save(reservation);
    }
}
