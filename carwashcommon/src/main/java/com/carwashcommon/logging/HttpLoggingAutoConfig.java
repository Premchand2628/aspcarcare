package com.carwashcommon.logging;

import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnClass({FilterRegistrationBean.class, Filter.class})
@ConditionalOnProperty(prefix = "carwash.http.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HttpLoggingAutoConfig {

  @Bean
  @ConditionalOnMissingBean(HttpRequestResponseLoggingFilter.class)
  public HttpRequestResponseLoggingFilter carwashCommonHttpRequestResponseLoggingFilter() {
    return new HttpRequestResponseLoggingFilter();
  }

  @Bean
  @ConditionalOnBean(HttpRequestResponseLoggingFilter.class)
  @ConditionalOnMissingBean(name = "httpRequestResponseLoggingFilterRegistration")
  public FilterRegistrationBean<HttpRequestResponseLoggingFilter> httpRequestResponseLoggingFilterRegistration(
      HttpRequestResponseLoggingFilter filter
  ) {
    FilterRegistrationBean<HttpRequestResponseLoggingFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(filter);
    registration.addUrlPatterns("/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
    return registration;
  }
}
