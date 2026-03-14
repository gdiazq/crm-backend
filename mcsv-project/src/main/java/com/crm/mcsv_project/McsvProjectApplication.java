package com.crm.mcsv_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class McsvProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(McsvProjectApplication.class, args);
	}

}
