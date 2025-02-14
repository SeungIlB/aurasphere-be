package com.elice.aurasphere.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@AllArgsConstructor
@Getter
public enum ErrorCode {

    // Success
    OK("S000", HttpStatus.OK, "OK"),

    //공통 에러
    MISSING_PART("C001", HttpStatus.BAD_REQUEST, "필수 데이터가 누락되었습니다."),

    //공통 서버에러
    INTERNAL_ERROR("E001", HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED("E002", HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP Method 요청입니다."),
    //프로필 관련 에러
    PROFILE_NOT_FOUND("E003", HttpStatus.NOT_FOUND, "프로필 정보를 찾을 수 없습니다."),

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
    CURRENT_PASSWORD_NOT_MATCH("U004", HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    NEW_PASSWORD_NOT_MATCH("U005", HttpStatus.BAD_REQUEST, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    INVALID_USER_REQUEST("U005", HttpStatus.BAD_REQUEST, "잘못된 사용자 요청입니다."),
    USER_NOT_MATCH("U006", HttpStatus.FORBIDDEN, "유저가 일치하지 않습니다."),

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
    POST_ALREADY_DELETED("P011", HttpStatus.NOT_FOUND, "삭제된 게시물입니다."),

    COMMENT_NOT_FOUND("C001", HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),

    IMAGE_NOT_FOUND("I001", HttpStatus.NOT_FOUND, "Key에 해당하는 이미지를 찾을 수 없습니다."),
    TOO_MANY_FILES("I002", HttpStatus.BAD_REQUEST, "파일 개수를 초과했습니다. 5장 미만으로 등록해주세요."),

    // 팔로우 관련 에러
    CANNOT_FOLLOW_YOURSELF("F001", HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),

    FILE_TYPE_NOT_SUPPORTED("F001", HttpStatus.BAD_REQUEST, "지원하지 않는 파일 타입입니다."),

    //조회수 관련 에러
    VIEW_UPDATE_FAILED("V001", HttpStatus.BAD_REQUEST, "조회수 업데이트에 실패했습니다."),

    // 알림 관련 에러
    NOTIFICATION_NOT_FOUND("N001", HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다."),
    NOTIFICATION_ACCESS_DENIED("N002", HttpStatus.FORBIDDEN, "해당 알림에 대한 접근 권한이 없습니다."),
    INVALID_NOTIFICATION_REQUEST("N003", HttpStatus.BAD_REQUEST, "알림 생성 요청이 유효하지 않습니다."),
    NOTIFICATION_TYPE_NULL("N004", HttpStatus.BAD_REQUEST, "알림 유형이 null일 수 없습니다."),
    NOTIFICATION_SAME_USER("N005", HttpStatus.BAD_REQUEST,"본인에게 알림을 보낼 수 없습니다."),
    ;

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    public String getDetailMessage(Throwable throwable) {
        if(throwable != null && !isBlank(throwable.getMessage())){
            log.info("throwable.getMessage {}", throwable.getMessage());
            return sanitizeMessage(this.message + " - " + throwable.getMessage());
        }
        return sanitizeMessage(this.message);
    }

    public String sanitizeMessage(String msg) {
        return Optional.ofNullable(msg)
                .filter(Predicate.not(String::isBlank))
                .orElse(this.message);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
