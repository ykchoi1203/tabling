package com.younggeun.tabling.service;

import com.younggeun.tabling.exception.impl.*;
import com.younggeun.tabling.model.constants.ReservationStatus;
import com.younggeun.tabling.persist.*;
import com.younggeun.tabling.persist.dto.ReviewDto;
import com.younggeun.tabling.persist.entity.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PartnerRepository partnerRepository;

    // 리뷰 작성
    @Transactional
    public ReviewEntity writeReview(Authentication authentication, ReviewDto reviewDto) {
        ReservationEntity reservation = reservationRepository.getById(reviewDto.getReservationId());

        UserEntity user = userRepository.findByUserId(authentication.getName()).orElseThrow(NoUserException::new);

        if(!Objects.equals(user.getId(), reservation.getUser().getId())) {
            throw new MismatchUserException();
        }

        if(reservation.getStatus() != ReservationStatus.ARRIVAL) {
            throw new NotAlreadyReservationException();
        }

        if(reviewRepository.countByReservationId(reservation.getId())>0) {
            throw new AlreadyExistReviewException();
        }

        ReviewEntity review = ReviewEntity.builder()
                                            .store(reservation.getStore())
                                            .reviewText(reviewDto.getReviewText())
                                            .starRating(reviewDto.getStarRating())
                                            .reservation(reservation)
                                            .user(user)
                                            .build();

        review = reviewRepository.save(review);


        updateStore(review, reservation.getStore());

        return review;

    }

    // 스토어 별점 update
    private void updateStore(ReviewEntity review, StoreEntity store) {
        store.setTotalReview(store.getTotalReview()+1);
        store.setTotalStarRating(store.getTotalStarRating() + review.getStarRating());

        storeRepository.save(store);
    }

    // 리뷰 삭제 (작성자)
    @Transactional
    public boolean deleteReviewByUser(Authentication authentication, ReviewDto reviewDto) {
        ReservationEntity reservation = reservationRepository.getById(reviewDto.getReviewId());

        UserEntity user = userRepository.findByUserId(authentication.getName()).orElseThrow(NoUserException::new);

        if(!Objects.equals(user.getId(), reservation.getUser().getId())) {
            throw new MismatchUserException();
        }

        ReviewEntity review = reviewRepository.findById(reviewDto.getReviewId()).orElseThrow(NoReviewException::new);
        StoreEntity store = review.getStore();

        store.setTotalReview(store.getTotalReview()-1);
        store.setTotalStarRating(store.getTotalStarRating() - review.getStarRating());
        updateStore(store);

        reviewRepository.deleteById(reviewDto.getReviewId());
        return true;
    }

    // 리뷰 삭제 (Partner)
    @Transactional
    public boolean deleteReviewByPartner(Authentication authentication, ReviewDto reviewDto) {
        ReservationEntity reservation = reservationRepository.getById(reviewDto.getReviewId());

        PartnerEntity partner = partnerRepository.findByPartnerId(authentication.getName()).orElseThrow(NoUserException::new);

        StoreEntity store = storeRepository.findById(reservation.getStore().getId()).orElseThrow(NoStoreException::new);

        if(!Objects.equals(partner.getId(), reservation.getUser().getId())) {
            throw new MismatchPartnerException();
        }

        if(!Objects.equals(store.getPartner().getPartnerId(), partner.getPartnerId())) {
            throw new MismatchStorePartnerException();
        }

        ReviewEntity review = reviewRepository.findById(reviewDto.getReviewId()).orElseThrow(NoReviewException::new);
        store = review.getStore();

        store.setTotalReview(store.getTotalReview()-1);
        store.setTotalStarRating(store.getTotalStarRating() - review.getStarRating());
        updateStore(store);

        reviewRepository.deleteById(review.getId());

        return true;
    }


    // 리뷰 수정
    @Transactional
    public ReviewEntity updateReview(Authentication authentication, ReviewDto reviewDto) {
        ReviewEntity review = reviewRepository.findById(reviewDto.getReviewId()).orElseThrow(NoReviewException::new);

        UserEntity user = userRepository.findByUserId(authentication.getName()).orElseThrow(NoUserException::new);

        StoreEntity store = review.getStore();

        if(!Objects.equals(user.getId(), review.getUser().getId())) {
            throw new MismatchUserException();
        }

        store.setTotalStarRating(store.getTotalStarRating() - review.getStarRating() + reviewDto.getStarRating());
        review.setStarRating(reviewDto.getStarRating());
        review.setReviewText(reviewDto.getReviewText());

        review.setStore(store);

        reviewRepository.save(review);

        updateStore(store);

        return review;

    }


    // store 수정 ( 별점 )
    private void updateStore(StoreEntity store) {
        storeRepository.save(store);
    }


    // 내가 쓴 리뷰 리스트
    public Page<ReviewEntity> selectReview(Authentication authentication, Pageable pageable) {
        UserEntity user = userRepository.findByUserId(authentication.getName()).orElseThrow(NoUserException::new);

        return reviewRepository.findByUserId(user.getId(), pageable);
    }


}
