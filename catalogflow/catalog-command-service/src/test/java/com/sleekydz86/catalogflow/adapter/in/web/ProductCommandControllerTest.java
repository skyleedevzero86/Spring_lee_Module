package com.sleekydz86.catalogflow.adapter.in.web;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.OutboxEventJpaRepository;
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
class ProductCommandControllerTest {

    private static final UUID CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUPPLIER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;

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
    void shouldCreateProduct() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-create-001")
                        .content("""
                                {
                                  "name": "\uBB34\uC120 \uD0A4\uBCF4\uB4DC",
                                  "description": "\uC800\uC18C\uC74C \uD0A4\uBCF4\uB4DC",
                                  "priceAmount": 59000,
                                  "priceCurrency": "KRW",
                                  "categoryId": "%s",
                                  "supplierId": "%s"
                                }
                                """.formatted(CATEGORY_ID, SUPPLIER_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-create-001"))
                .andExpect(jsonPath("$.name", is("\uBB34\uC120 \uD0A4\uBCF4\uB4DC")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.version", is(0)));

        long outboxCount = outboxEventJpaRepository.findAll().stream()
                .filter(event -> "ProductCreated".equals(event.getEventType()))
                .count();
        org.junit.jupiter.api.Assertions.assertTrue(outboxCount >= 1);
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "\uC124\uBA85",
                                  "priceAmount": -1,
                                  "priceCurrency": "KRW",
                                  "categoryId": "%s",
                                  "supplierId": "%s"
                                }
                                """.formatted(CATEGORY_ID, SUPPLIER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("\uC694\uCCAD \uAC80\uC99D \uC2E4\uD328")));
    }

    @Test
    void shouldChangeProductPrice() throws Exception {
        String createResponse = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "USB \uD5C8\uBE0C",
                                  "description": "7\uD3EC\uD2B8",
                                  "priceAmount": 25000,
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

        mockMvc.perform(patch("/api/v1/products/{productId}/price", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "version": 0,
                                  "priceAmount": 19900,
                                  "priceCurrency": "KRW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version", is(1)));
    }
}
