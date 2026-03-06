package com.homeprotectors.backend.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<UserIdHeaderFilter> userIdHeaderFilter() {
        FilterRegistrationBean<UserIdHeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new UserIdHeaderFilter());
        bean.setOrder(1); // 먼저 타게
        return bean;
    }
}
