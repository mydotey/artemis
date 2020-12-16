package org.mydotey.artemis.web;

import java.util.HashMap;
import java.util.Map;

import org.mydotey.caravan.web.filter.CrossDomainFilter;
import org.mydotey.caravan.web.filter.Filters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import com.github.ziplet.filter.compression.CompressingFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<CrossDomainFilter> filterRegistration() {
        FilterRegistrationBean<CrossDomainFilter> registration = new FilterRegistrationBean<>(
            (CrossDomainFilter) Filters.newCrossDomainFilter());
        registration.addUrlPatterns("/*");

        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("encoding", "UTF-8");
        initParameters.put("forceEncoding", "true");
        registration.setInitParameters(initParameters);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<CompressingFilter> filterRegistration2() {
        FilterRegistrationBean<CompressingFilter> registration = new FilterRegistrationBean<>(
            (CompressingFilter) Filters.newCompressingFilter());
        registration.addUrlPatterns("/*");

        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("encoding", "UTF-8");
        initParameters.put("forceEncoding", "true");
        registration.setInitParameters(initParameters);

        return registration;
    }

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> filterRegistration3() {
        FilterRegistrationBean<HiddenHttpMethodFilter> registration = new FilterRegistrationBean<>(
            new HiddenHttpMethodFilter());
        registration.addUrlPatterns("/*");

        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("encoding", "UTF-8");
        initParameters.put("forceEncoding", "true");
        registration.setInitParameters(initParameters);

        return registration;
    }

}
