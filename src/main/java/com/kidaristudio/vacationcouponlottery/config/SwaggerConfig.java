package com.kidaristudio.vacationcouponlottery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 * API 문서화를 위한 설정을 제공합니다.
 * 
 * 컨트롤러의 @Tag 어노테이션과 함께 사용하여 API 문서를 생성합니다.
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * OpenAPI 기본 정보 설정
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("휴가 쿠폰 추첨 시스템 API")
                        .description("직원들을 위한 휴가 쿠폰 추첨 시스템의 REST API 문서입니다.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("정태현(개발자)")
                                .email("chomman@naver.com")
                                .url("https://hyun-portfolio.vercel.app/"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("개발 서버"),
                        new Server()
                                .url("https://api.vacation-lottery.com")
                                .description("운영 서버")
                ));
    }
}