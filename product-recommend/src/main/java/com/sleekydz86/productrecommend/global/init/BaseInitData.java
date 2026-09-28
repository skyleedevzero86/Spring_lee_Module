package com.sleekydz86.productrecommend.global.init;

import com.sleekydz86.productrecommend.application.port.in.ProductUseCase;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class BaseInitData {

	@Bean
	@Profile("!test & !local & !it")
	ApplicationRunner initDataRunner(ProductUseCase productUseCase) {
		return args -> {
			if (!productUseCase.findAll().isEmpty()) {
				return;
			}
			productUseCase.create("맥북 프로 16", List.of("노트북", "애플", "개발"), "LAPTOP", "APPLE", "SILVER", BigDecimal.valueOf(3_200_000), 8);
			productUseCase.create("맥북 에어 M3", List.of("노트북", "애플", "경량"), "LAPTOP", "APPLE", "SILVER", BigDecimal.valueOf(1_890_000), 12);
			productUseCase.create("델 XPS 15", List.of("노트북", "윈도우", "고성능"), "LAPTOP", "DELL", "BLACK", BigDecimal.valueOf(2_100_000), 6);
			productUseCase.create("나이키 페가수스 41", List.of("러닝", "운동화", "쿠션"), "RUNNING_SHOES", "NIKE", "BLACK", BigDecimal.valueOf(139_000), 40);
			productUseCase.create("아디다스 울트라부스트", List.of("러닝", "운동화", "쿠션"), "RUNNING_SHOES", "ADIDAS", "BLACK", BigDecimal.valueOf(189_000), 25);
			productUseCase.create("뉴발란스 1080", List.of("러닝", "운동화", "경량"), "RUNNING_SHOES", "NEW_BALANCE", "BLACK", BigDecimal.valueOf(169_000), 18);
			productUseCase.create("아식스 님버스", List.of("러닝", "쿠션", "장거리"), "RUNNING_SHOES", "ASICS", "BLUE", BigDecimal.valueOf(199_000), 14);
			productUseCase.create("호카 클리프톤", List.of("러닝", "경량", "데일리"), "RUNNING_SHOES", "HOKA", "BLACK", BigDecimal.valueOf(219_000), 10);
			productUseCase.create("에어팟 프로 2", List.of("이어폰", "애플", "노이즈캔슬링"), "AUDIO", "APPLE", "WHITE", BigDecimal.valueOf(359_000), 30);
			productUseCase.create("소니 WH-1000XM5", List.of("헤드폰", "노이즈캔슬링"), "AUDIO", "SONY", "BLACK", BigDecimal.valueOf(449_000), 15);
		};
	}
}
