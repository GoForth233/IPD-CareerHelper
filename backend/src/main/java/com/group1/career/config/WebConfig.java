package com.group1.career.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final com.group1.career.interceptor.AuthInterceptor authInterceptor;

    public WebConfig(com.group1.career.interceptor.AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login", "/auth/register", "/auth/wechat-login",
                        "/auth/send-code", "/auth/reset-password", "/auth/check-email",
                        "/api/homepage/**",
                        "/api/careers/**",
                        "/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**"
                );
    }
}

