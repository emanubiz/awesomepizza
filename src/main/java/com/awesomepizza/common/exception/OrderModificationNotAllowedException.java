package com.awesomepizza.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) 
public class OrderModificationNotAllowedException extends RuntimeException {
    public OrderModificationNotAllowedException(String message) {
        super(message);
    }
}
