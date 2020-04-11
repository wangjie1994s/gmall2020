package com.aisino.gmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.aisino.gmall.user.mapper")
public class GmallUserApplication {

	public static void main(String[] args) {

		SpringApplication.run(GmallUserApplication.class, args);
		System.out.println("============================SpringBoot启动成功============================");
	}

}
