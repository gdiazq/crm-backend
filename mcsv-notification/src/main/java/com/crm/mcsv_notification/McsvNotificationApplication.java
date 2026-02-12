package com.crm.mcsv_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class McsvNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(McsvNotificationApplication.class, args);
	}

}
