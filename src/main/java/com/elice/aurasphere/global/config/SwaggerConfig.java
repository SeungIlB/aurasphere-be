package com.elice.aurasphere.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String securityDescription = """
            "AuraSphere 애플리케이션의 API 문서입니다."
            
            ## 공통 인증 에러 응답
            
            모든 인증이 필요한 API는 다음과 같은 에러 응답이 발생할 수 있습니다.
            
            ### 401 Unauthorized
            * 인증 토큰이 존재하지 않는 경우
              ```json
              {
                "status": "UNAUTHORIZED",
                "message": "인증 토큰이 존재하지 않습니다.",
                "data": null
              }
              ```
            * 토큰이 만료된 경우
              ```json
              {
                "status": "UNAUTHORIZED",
                "message": "만료된 토큰입니다. 토큰을 재발급 받아주세요.",
                "data": null
              }
              ```
            * 토큰이 유효하지 않은 경우
              ```json
              {
                "status": "UNAUTHORIZED",
                "message": "유효하지 않은 토큰입니다.",
                "data": null
              }
              ```
            * 리프레시 토큰이 없는 경우
              ```json
              {
                "status": "UNAUTHORIZED",
                "message": "리프레시 토큰이 존재하지 않습니다.",
                "data": null
              }
              ```
            
            ### 403 Forbidden
            * 권한이 없는 경우
              ```json
              {
                "status": "FORBIDDEN",
                "message": "해당 리소스에 대한 권한이 없습니다.",
                "data": null
              }
              ```

            ## 토큰 갱신
            액세스 토큰 만료 시 리프레시 토큰을 사용하여 자동으로 재발급을 시도합니다.
            재발급 성공 시 쿠키가 자동으로 갱신됩니다.
            """;
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")))
                .info(new Info()
                        .title("AuraSphere API")
                        .version("1.0")
                        .description(securityDescription));
    }
}