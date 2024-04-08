package com.younggeun.tabling.service;

import com.younggeun.tabling.exception.impl.*;
import com.younggeun.tabling.model.Auth;
import com.younggeun.tabling.model.constants.ReservationStatus;
import com.younggeun.tabling.persist.ReservationRepository;
import com.younggeun.tabling.persist.StoreRepository;
import com.younggeun.tabling.persist.UserRepository;
import com.younggeun.tabling.persist.dto.ReservationDto;
import com.younggeun.tabling.persist.dto.StoreDto;
import com.younggeun.tabling.persist.entity.ReservationEntity;
import com.younggeun.tabling.persist.entity.StoreEntity;
import com.younggeun.tabling.persist.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return this.userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다 -> " + userId));
    }

    // 회원가입
    public UserEntity register(Auth.SignUp user) {
        if(this.userRepository.existsByUserId(user.getUserId())) {
            throw new AlreadyExistUserException();
        }

        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        return this.userRepository.save(user.toEntity());
    }

    // 로그인
    public UserEntity authenticate(Auth.SignIn user) {
        var member = this.userRepository.findByUserId(user.getUserId()).orElseThrow(NoUserException::new);

        if(!this.passwordEncoder.matches(user.getPassword(), member.getPassword())) {
            throw new PasswordMismatchException();
        }
        return member;
    }

    // 상점 목록 ( 가나다순, 거리순, 별점순 )
    public Page<StoreDto> getAllStores(Pageable pageable, String orderBy, double lat, double lon) {
        if(orderBy.isEmpty() || orderBy.equals("가나다순")) {
            return this.storeRepository.findAllByOrderByStoreName(pageable);
        } else if(orderBy.equals("거리순")) {
            return this.storeRepository.findAllByOrderByDistance(pageable, lat, lon);
        } else if(orderBy.equals("평점순")) {
            return this.storeRepository.findAllByOrderByStarRating(pageable);
        }

        return this.storeRepository.findAllByOrderByStoreName(pageable);
    }

    // 상점 상세보기
    public StoreEntity getStoreDetail(Long storeId) {
        return this.storeRepository.findById(storeId).orElseThrow(NoStoreException::new);
    }


    // 상점 예약
    public ReservationEntity reservation(Authentication authentication, ReservationDto reservationDto) {
        UserEntity user = this.userRepository.findByUserId(authentication.getName()).orElseThrow(NoUserException::new);
        StoreEntity store = this.storeRepository.findById(reservationDto.getStoreId()).orElseThrow(NoStoreException::new);

        LocalDate date = reservationDto.getDate(); // 예약 날짜
        LocalTime time = reservationDto.getTime(); // 예약 시간

        LocalDateTime reservationTime = new LocalDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                time.getHour(), time.getMinute(), time.getSecond());
        if(LocalDateTime.now().isAfter(reservationTime)) {
            throw new CannotReservationBeforeTimeException();
        }

        return reservationRepository.save(ReservationEntity.builder()
                .store(store)
                .date(reservationDto.getDate())
                .time(reservationDto.getTime())
                .user(user)
                .status(ReservationStatus.WAIT)
                .build());
    }

}
