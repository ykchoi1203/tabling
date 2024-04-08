package com.younggeun.tabling.exception.impl;

import com.younggeun.tabling.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyExistReservationException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "이미 처리된 예약내역입니다.";
    }
}
