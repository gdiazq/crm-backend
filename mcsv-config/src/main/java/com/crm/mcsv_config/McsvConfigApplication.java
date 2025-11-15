package com.crm.mcsv_config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class McsvConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(McsvConfigApplication.class, args);
	}

}