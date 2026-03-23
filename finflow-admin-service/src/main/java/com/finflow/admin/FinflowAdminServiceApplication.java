package com.finflow.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FinflowAdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinflowAdminServiceApplication.class, args);
	}

}
