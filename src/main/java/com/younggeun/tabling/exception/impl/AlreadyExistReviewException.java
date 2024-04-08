package com.younggeun.tabling.exception.impl;

import com.younggeun.tabling.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyExistReviewException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "이미 작성한 리뷰가 있습니다.";
    }
}
