package com.sleekydz86.productrecommend.global.init;

import com.sleekydz86.productrecommend.application.port.in.ProductUseCase;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
			productUseCase.create("맥북 프로 16", List.of("노트북", "애플", "개발", "프로그래밍"));
			productUseCase.create("맥북 에어 M3", List.of("노트북", "애플", "경량", "휴대용"));
			productUseCase.create("델 XPS 15", List.of("노트북", "윈도우", "개발", "고성능"));
			productUseCase.create("아이폰 15 프로", List.of("스마트폰", "애플", "카메라", "5G"));
			productUseCase.create("갤럭시 S24", List.of("스마트폰", "안드로이드", "카메라", "AI"));
			productUseCase.create("아이패드 프로 12.9", List.of("태블릿", "애플", "드로잉", "크리에이티브"));
			productUseCase.create("소니 WH-1000XM5", List.of("헤드폰", "무선", "노이즈캔슬링", "음악"));
			productUseCase.create("에어팟 프로 2", List.of("이어폰", "애플", "무선", "노이즈캔슬링"));
			productUseCase.create("노스페이스 패딩", List.of("아우터", "겨울", "보온", "아웃도어"));
			productUseCase.create("파타고니아 플리스", List.of("아우터", "플리스", "아웃도어", "하이킹"));
			productUseCase.create("리바이스 501", List.of("바지", "데님", "캐주얼", "클래식"));
			productUseCase.create("나이키 에어맥스 90", List.of("신발", "스니커즈", "러닝", "스포츠"));
			productUseCase.create("아디다스 울트라부스트", List.of("신발", "러닝", "스포츠", "편안함"));
			productUseCase.create("스타벅스 파이크 플레이스", List.of("커피", "원두", "음료", "카페인"));
			productUseCase.create("트와이닝스 얼그레이", List.of("차", "얼그레이", "음료", "홍차"));
			productUseCase.create("코카콜라 제로", List.of("탄산", "음료", "제로슈거", "청량"));
			productUseCase.create("다이슨 V15", List.of("청소기", "청소", "생활가전", "무선"));
			productUseCase.create("인스턴트팟 듀오", List.of("쿠커", "주방", "압력솥", "다기능"));
			productUseCase.create("네스프레소 버츄오", List.of("커피머신", "주방", "에스프레소", "캡슐"));
			productUseCase.create("클린 코드", List.of("도서", "프로그래밍", "소프트웨어", "교육"));
		};
	}
}
