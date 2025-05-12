package com.nsoft.integrations.vibra.kanban_api.api.config;

import com.nsoft.integrations.vibra.kanban_api.api.filter.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
    FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new RateLimitingFilter());
    registration.addUrlPatterns("/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
