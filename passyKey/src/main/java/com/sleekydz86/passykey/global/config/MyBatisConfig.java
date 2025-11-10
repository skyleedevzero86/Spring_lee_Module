package com.sleekydz86.passykey.global.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.sleekydz86.passykey.adapter.outbound.persistence")
public class MyBatisConfig {
}

