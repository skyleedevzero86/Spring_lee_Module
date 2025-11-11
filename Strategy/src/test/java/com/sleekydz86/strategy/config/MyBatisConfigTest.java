package com.sleekydz86.strategy.config;

import com.sleekydz86.strategy.global.config.MyBatisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyBatisConfigTest {

    @Test
    @DisplayName("MyBatis 설정 클래스 존재 여부 확인")
    void testMyBatisConfigExists() {
        // given & when
        MyBatisConfig config = new MyBatisConfig();
        
        // then
        assertNotNull(config);
    }

    @Test
    @DisplayName("MyBatis 설정 클래스에 @Configuration 어노테이션 존재 확인")
    void testMyBatisConfigAnnotation() {
        // given & when & then
        assertTrue(MyBatisConfig.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
    }

    @Test
    @DisplayName("MyBatis 설정 클래스에 @MapperScan 어노테이션 존재 확인")
    void testMyBatisConfigMapperScan() {
        // given & when & then
        assertTrue(MyBatisConfig.class.isAnnotationPresent(org.mybatis.spring.annotation.MapperScan.class));
    }
}

