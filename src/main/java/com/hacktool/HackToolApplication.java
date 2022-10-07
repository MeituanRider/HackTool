package com.hacktool;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hacktool.mapper")
public class HackToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(HackToolApplication.class, args);
    }

}
