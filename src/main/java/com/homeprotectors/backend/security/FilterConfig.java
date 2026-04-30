package com.homeprotectors.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeprotectors.backend.service.JwtTokenService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter(
            ObjectMapper objectMapper,
            JwtTokenService jwtTokenService
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtAuthenticationFilter(objectMapper, jwtTokenService));
        bean.setOrder(1);
        return bean;
    }
}
