package kr.lawhan.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * M2 시점: 공개 조회 API(/api/listings/**)만 존재하므로 전체 permitAll.
 * spring-boot-starter-security 기본값(전체 인증 요구 + 매 기동마다 임시 비밀번호 생성)을
 * 덮어써 공개 API를 인증 없이 열어둔다. /admin/** 인증 가드는 M4에서 별도로 추가한다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
