package com.homeprotectors.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<UserIdHeaderFilter> userIdHeaderFilter(ObjectMapper objectMapper) {
        FilterRegistrationBean<UserIdHeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new UserIdHeaderFilter(objectMapper));
        bean.setOrder(1);
        return bean;
    }
}
