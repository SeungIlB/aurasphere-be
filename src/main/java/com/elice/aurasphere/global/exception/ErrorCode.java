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
    INTERNAL_ERROR("E001", HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED("E002", HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP Method 요청입니다."),

    USER_NOT_FOUND("U001", HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    USER_NOT_MATCH("U002", HttpStatus.FORBIDDEN, "유저가 일치하지 않습니다."),

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
