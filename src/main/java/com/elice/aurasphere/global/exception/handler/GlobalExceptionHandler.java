package com.elice.aurasphere.global.exception.handler;

import com.elice.aurasphere.global.common.ApiResponseDto;
import com.elice.aurasphere.global.exception.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
public class GlobalExceptionHandler {

    // JWT 관련 예외 처리
    @ExceptionHandler(ExpiredJwtException.class)
    protected ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("ExpiredJwtException : {}", e.getMessage());
        return handleExceptionInternal(ErrorCode.TOKEN_EXPIRED);
    }


    @ExceptionHandler(SignatureException.class)
    protected ResponseEntity<Object> handleSignatureException(SignatureException e) {
        log.error("SignatureException : {}", e.getMessage());
        return handleExceptionInternal(ErrorCode.TAMPERED_TOKEN);
    }

    @ExceptionHandler(MalformedJwtException.class)
    protected ResponseEntity<Object> handleMalformedJwtException(MalformedJwtException e) {
        log.error("MalformedJwtException : {}", e.getMessage());
        return handleExceptionInternal(ErrorCode.INVALID_TOKEN);
    }

    @ExceptionHandler(JwtException.class)
    protected ResponseEntity<Object> handleJwtException(JwtException e) {
        log.error("JwtException : {}", e.getMessage());
        return handleExceptionInternal(ErrorCode.INVALID_TOKEN);
    }

    // 인증 관련 예외 처리
    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException e) {
        log.error("BadCredentialsException : {}", e.getMessage());
        return handleExceptionInternal(ErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    protected ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException e) {
        log.error("UsernameNotFoundException : {}", e.getMessage());
        return handleExceptionInternal(ErrorCode.USER_NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<Object> handleAuthenticationException(AuthenticationException e) {
        log.error("AuthenticationException : {}", e.getMessage());
        return handleExceptionInternal(ErrorCode.INVALID_CREDENTIALS);
    }

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex) {
        log.error(ex.toString(), ex);
        return handleExceptionInternal(ex.getErrorCode());
    }

//    @ExceptionHandler(MissingServletRequestPartException.class)
//    public ResponseEntity<Object> handleMissingPartException(MissingServletRequestPartException ex) {
//        String message = "필수 데이터를 포함해주세요 : " + ex.getRequestPartName();
//        return handleExceptionInternal(ErrorCode.MISSING_PART, message);
//    }


    //Valid Exception 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException : {}", e.getMessage());
        ValidExceptionResponse exceptionResponse = makeErrorResponse(e);
        return handleExceptionInternal(ErrorCode.MISSING_PART, exceptionResponse);
    }

    // 지원하지 않는 HTTP method를 호출할 경우
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException : {}", e.getMessage());
        return handleExceptionInternal(ErrorCode.METHOD_NOT_ALLOWED);
    }

    // 그 밖에 발생하는 모든 예외 처리
    @ExceptionHandler(value = {Exception.class, RuntimeException.class, SQLException.class, DataIntegrityViolationException.class})
    protected ResponseEntity<Object> handleException(Exception e) {
        log.error("server error {}", e.toString(), e);
        return handleExceptionInternal(ErrorCode.INTERNAL_ERROR, e);
    }

    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponseDto.from(errorCode));
    }

    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode, ValidExceptionResponse validExceptionResponse) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ValidErrorResponseDto.of(errorCode, validExceptionResponse));
    }

    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode, Exception e) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponseDto.of(errorCode, e));
    }

    //Valid 유효성 검사 에러 리스트 받아서 받환
    private ValidExceptionResponse makeErrorResponse(BindException e) {
        List<ValidExceptionResponse.ValidationError> validationErrorList = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ValidExceptionResponse.ValidationError::of)
                .collect(Collectors.toList());

        return ValidExceptionResponse.builder()
                .errors(validationErrorList)
                .build();
    }

}