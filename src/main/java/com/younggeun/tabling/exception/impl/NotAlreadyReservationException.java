package com.younggeun.tabling.exception.impl;

import com.younggeun.tabling.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NotAlreadyReservationException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "완료되지 않은 예약입니다.";
    }
}
