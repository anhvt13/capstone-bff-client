package com.bff.client.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bff.client.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = ClientAuthorizationException.class)
    public ResponseEntity<?> handleUnauthorizedException(ClientAuthorizationException e) {
        ErrorResponse oauthError = new ErrorResponse(LocalDateTime.now(), e.getMessage());
        return new ResponseEntity<>(oauthError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity<?> handleClientException(HttpClientErrorException e) {
        ErrorResponse clientError = new ErrorResponse(LocalDateTime.now(), e.getMessage());
        return new ResponseEntity<>(clientError, HttpStatus.FORBIDDEN);
    }

}

