package com.crm.mcsv_rrhh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.crm.mcsv_rrhh", "com.crm.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaRepositories(basePackages = {"com.crm.mcsv_rrhh", "com.crm.common.storage.repository"})
@EntityScan(basePackages = {"com.crm.mcsv_rrhh", "com.crm.common.storage.entity"})
public class McsvRrhhApplication {

	public static void main(String[] args) {
		SpringApplication.run(McsvRrhhApplication.class, args);
	}

}
