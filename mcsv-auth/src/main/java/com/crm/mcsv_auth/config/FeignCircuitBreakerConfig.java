package com.crm.mcsv_auth.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableFeignClients(basePackages = "com.crm.mcsv_auth.client")
@Import(FeignClientsConfiguration.class)
public class FeignCircuitBreakerConfig {
}
