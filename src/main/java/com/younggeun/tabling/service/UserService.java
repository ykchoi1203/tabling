package com.younggeun.tabling.service;

import com.younggeun.tabling.model.Auth;
import com.younggeun.tabling.model.constants.ReservationStatus;
import com.younggeun.tabling.persist.ReservationRepository;
import com.younggeun.tabling.persist.StoreRepository;
import com.younggeun.tabling.persist.UserRepository;
import com.younggeun.tabling.persist.dto.ReservationDto;
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
            throw new RuntimeException("이미 사용중인 아이디 입니다.");
        }

        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        return this.userRepository.save(user.toEntity());
    }

    // 로그인
    public UserEntity authenticate(Auth.SignIn user) {
        var member = this.userRepository.findByUserId(user.getUserId()).orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));

        if(!this.passwordEncoder.matches(user.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    // 상점 목록 ( 가나다순, 거리순, 별점순 )
    public Page<StoreEntity> getAllStores(Pageable pageable, String orderBy, double lat, double lon) {
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
        return this.storeRepository.findById(storeId).orElseThrow(() -> new RuntimeException("존재하지 않는 매장입니다."));
    }


    // 상점 예약
    public ReservationEntity reservation(Authentication authentication, ReservationDto reservationDto) {
        UserEntity user = this.userRepository.findByUserId(authentication.getName()).orElseThrow(() -> new RuntimeException("유효한 유저가 아닙니다."));
        StoreEntity store = this.storeRepository.findById(reservationDto.getStoreId()).orElseThrow(()-> new RuntimeException("해당 매장 정보가 존재하지 않습니다."));

        LocalDate date = reservationDto.getDate(); // 예약 날짜
        LocalTime time = reservationDto.getTime(); // 예약 시간

        LocalDateTime reservationTime = new LocalDateTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                time.getHour(), time.getMinute(), time.getSecond());
        if(LocalDateTime.now().isAfter(reservationTime)) {
            throw new RuntimeException("현재 시간보다 전 시간으로 예약할 수 없습니다.");
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
