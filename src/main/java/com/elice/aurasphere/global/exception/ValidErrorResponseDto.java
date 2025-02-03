package com.elice.aurasphere.global.exception;

import com.elice.aurasphere.global.common.ValidResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidErrorResponseDto extends ValidResponseDTO {

    private ValidErrorResponseDto(ErrorCode errorCode, ValidExceptionResponse validExceptionResponse) {
        super(errorCode.getCode(), validExceptionResponse);
    }

    public static ValidErrorResponseDto of(ErrorCode errorCode, ValidExceptionResponse validExceptionResponse) {
        return new ValidErrorResponseDto(errorCode, validExceptionResponse);
    }




}
