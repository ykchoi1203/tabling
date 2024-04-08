package com.younggeun.tabling.exception.impl;

import com.younggeun.tabling.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class MismatchStorePartnerException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "해당 매장의 관리자가 아닙니다.";
    }
}
