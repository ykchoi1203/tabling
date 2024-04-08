package com.younggeun.tabling.exception.impl;

import com.younggeun.tabling.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class CannotReservationBeforeTimeException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "현재 시간보다 전 시간으로 예약할 수 없습니다.";
    }
}
