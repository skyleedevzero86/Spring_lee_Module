package com.sleekydz86.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StrategyApplicationTests {

    @Test
    @DisplayName("Spring 컨텍스트 로드 확인")
    void contextLoads() {
        System.out.println("컨텍스트 로드 됩니당!!!");
    }

}
