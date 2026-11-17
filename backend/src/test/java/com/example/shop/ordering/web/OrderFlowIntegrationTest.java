package com.example.shop.ordering.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test: HTTP → controller → application service → domain → JPA → H2 (with Flyway
 * migrations) → domain events → Catalog stock reservation. Few of these; they are slow but
 * prove the wiring.
 *
 * <p>The Spring context (and its in-memory H2 database) is shared across tests, so each test
 * leaves stock as it found it. For full isolation use Testcontainers + @Transactional tests or
 * truncate tables in @BeforeEach (see docs → Testing).
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowIntegrationTest {

    private static final String DDD_BOOK = "11111111-1111-1111-1111-000000000001";
    private static final String STICKERS = "11111111-1111-1111-1111-000000000005"; // only 2 in stock

    @Autowired
    MockMvc mvc;

    @Test
    void placing_an_order_reserves_stock_and_cancelling_releases_it() throws Exception {
        String orderId = startOrder();

        mvc.perform(post("/api/orders/{id}/lines", orderId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + DDD_BOOK + "\",\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(164.97));

        mvc.perform(post("/api/orders/{id}/place", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLACED"));

        mvc.perform(get("/api/products/{id}", DDD_BOOK))
                .andExpect(jsonPath("$.stockOnHand").value(9)); // 12 - 3

        mvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mvc.perform(get("/api/products/{id}", DDD_BOOK))
                .andExpect(jsonPath("$.stockOnHand").value(12));
    }

    @Test
    void overselling_rolls_back_the_whole_placement() throws Exception {
        String orderId = startOrder();
        mvc.perform(post("/api/orders/{id}/lines", orderId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + STICKERS + "\",\"quantity\":5}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/orders/{id}/place", orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Insufficient stock")));

        // Order status change was rolled back together with the failed reservation.
        mvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void invalid_request_returns_problem_detail() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerEmail\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    private String startOrder() throws Exception {
        String body = mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerEmail\":\"ada@example.com\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }
}
