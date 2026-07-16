package com.sleekydz86.catalogflow.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.storage.FakeStorageAdapter;
import com.sleekydz86.catalogflow.global.config.CorrelationIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class ProductImageCommandControllerTest {

	private static final UUID CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SUPPLIER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private FakeStorageAdapter fakeStorageAdapter;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.addFilters(webApplicationContext.getBean(CorrelationIdFilter.class))
				.build();
	}

	@Test
	void shouldCreatePresignedUrlAndRegisterUploadedImage() throws Exception {
		String createResponse = mockMvc.perform(post("/api/v1/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "이미지 상품",
								  "description": "설명",
								  "priceAmount": 12000,
								  "priceCurrency": "KRW",
								  "categoryId": "%s",
								  "supplierId": "%s"
								}
								""".formatted(CATEGORY_ID, SUPPLIER_ID)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String productId = createResponse.split("\"productId\":\"")[1].split("\"")[0];

		String presignResponse = mockMvc.perform(post("/api/v1/products/{productId}/images/presigned-url", productId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "contentType": "image/png",
								  "sizeInBytes": 2048,
								  "fileName": "cover.png",
								  "temporary": true
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.storageKey", startsWith("temp/" + productId + "/")))
				.andExpect(jsonPath("$.uploadUrl", containsString("/fake-storage/upload/")))
				.andExpect(jsonPath("$.contentType", is("image/png")))
				.andExpect(jsonPath("$.temporary", is(true)))
				.andReturn()
				.getResponse()
				.getContentAsString();

		String storageKey = presignResponse.split("\"storageKey\":\"")[1].split("\"")[0];
		fakeStorageAdapter.markUploaded(storageKey);

		mockMvc.perform(post("/api/v1/products/{productId}/images", productId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "version": 0,
								  "storageKey": "%s",
								  "contentType": "image/png",
								  "sizeInBytes": 2048,
								  "temporary": true
								}
								""".formatted(storageKey)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.version", is(1)));
	}

	@Test
	void shouldRejectInvalidPresignedUploadRequest() throws Exception {
		String createResponse = mockMvc.perform(post("/api/v1/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "검증 상품",
								  "description": "설명",
								  "priceAmount": 1000,
								  "priceCurrency": "KRW",
								  "categoryId": "%s",
								  "supplierId": "%s"
								}
								""".formatted(CATEGORY_ID, SUPPLIER_ID)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String productId = createResponse.split("\"productId\":\"")[1].split("\"")[0];

		mockMvc.perform(post("/api/v1/products/{productId}/images/presigned-url", productId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "contentType": "application/pdf",
								  "sizeInBytes": 2048,
								  "fileName": "file.pdf",
								  "temporary": true
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title", is("이미지 업로드 요청이 올바르지 않음")));
	}

	@Test
	void shouldRejectRegisterWhenObjectMissing() throws Exception {
		String createResponse = mockMvc.perform(post("/api/v1/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "미업로드 상품",
								  "description": "설명",
								  "priceAmount": 3000,
								  "priceCurrency": "KRW",
								  "categoryId": "%s",
								  "supplierId": "%s"
								}
								""".formatted(CATEGORY_ID, SUPPLIER_ID)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String productId = createResponse.split("\"productId\":\"")[1].split("\"")[0];
		String storageKey = "temp/" + productId + "/missingobject.png";

		mockMvc.perform(post("/api/v1/products/{productId}/images", productId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "version": 0,
								  "storageKey": "%s",
								  "contentType": "image/png",
								  "sizeInBytes": 1024,
								  "temporary": true
								}
								""".formatted(storageKey)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title", is("저장소 객체를 찾을 수 없음")));
	}
}
