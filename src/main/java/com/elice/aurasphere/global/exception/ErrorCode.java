package com.elice.aurasphere.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Predicate;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // Success
    OK("S000", HttpStatus.OK, "OK"),

    //공통 서버에러
    INTERNAL_ERROR("S001", HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED("S002", HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP Method 요청입니다."),

    // 인증 관련 에러
    TOKEN_EXPIRED("A001", HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN("A002", HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND("A003", HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),
    INVALID_CREDENTIALS("A004", HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다."),
    ACCESS_TOKEN_NOT_FOUND("A005", HttpStatus.UNAUTHORIZED, "액세스 토큰을 찾을 수 없습니다."),
    TAMPERED_TOKEN("A006", HttpStatus.BAD_REQUEST, "변조된 토큰입니다."),

    // 사용자 관련 에러
    USER_NOT_FOUND("U001", HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS("U002", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    NICKNAME_ALREADY_EXISTS("U003", HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    INVALID_PASSWORD("U004", HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다."),
    INVALID_USER_REQUEST("U005", HttpStatus.BAD_REQUEST, "잘못된 사용자 요청입니다."),

    // 이메일 인증 관련 에러
    VERIFICATION_CODE_NOT_FOUND("V001", HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    VERIFICATION_CODE_EXPIRED("V002", HttpStatus.BAD_REQUEST, "만료된 인증 코드입니다."),
    VERIFICATION_ALREADY_VERIFIED("V003", HttpStatus.BAD_REQUEST, "이미 인증이 완료된 코드입니다."),

    POST_NOT_FOUND("P001", HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    POST_INVALID_ID("P002", HttpStatus.BAD_REQUEST, "잘못된 게시글 ID입니다."),
    POST_NOT_AUTHORIZED("P003", HttpStatus.FORBIDDEN, "해당 게시글의 작성자가 아닙니다."),
    POST_CREATION_FAILED("P004", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 작성에 실패했습니다."),
    POST_UPDATE_FAILED("P005", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 수정에 실패했습니다."),
    POST_DELETION_FAILED("P006", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 삭제에 실패했습니다."),
    POST_ALREADY_EXISTS("P007", HttpStatus.CONFLICT, "이미 존재하는 게시글입니다."),
    POST_RETRIEVAL_FAILED("P008", HttpStatus.INTERNAL_SERVER_ERROR, "게시글을 조회하는 데 실패했습니다."),
    POST_IMAGE_UPLOAD_FAILED("P009", HttpStatus.INTERNAL_SERVER_ERROR, "게시글 이미지 업로드에 실패했습니다."),
    POST_SERVER_ERROR("P010", HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 게시글을 작성할 수 없습니다."),

    IMAGE_NOT_FOUND("I001", HttpStatus.NOT_FOUND, "Key에 해당하는 이미지를 찾을 수 없습니다.")

            ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    public String getMessage(Throwable throwable) {
        return this.getMessage(this.getMessage(this.getMessage() + " - " + throwable.getMessage()));
    }

    public String getMessage(String message) {
        return Optional.ofNullable(message)
                .filter(Predicate.not(String::isBlank))
                .orElse(this.getMessage());
    }
}
