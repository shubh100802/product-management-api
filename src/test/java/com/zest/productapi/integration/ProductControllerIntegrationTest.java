package com.zest.productapi.integration;

// ==========file-context==========

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.productapi.dto.LoginRequest;
import com.zest.productapi.dto.ProductCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void productsEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userToken_shouldNotCreateProduct() throws Exception {
        String userEmail = "user" + System.currentTimeMillis() + "@p.com";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.zest.productapi.dto.RegisterRequest("Normal User", userEmail, "Password@123")
                        )))
                .andExpect(status().isCreated());

        String userToken = loginAndGetAccessToken(userEmail, "Password@123");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductCreateRequest("Laptop", "admin"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminToken_shouldCreateAndUserShouldRead() throws Exception {
        String adminToken = loginAndGetAccessToken("admin@test.local", "Admin@123");

        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductCreateRequest("Monitor", "admin"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long productId = createJson.path("data").path("id").asLong();

        String userEmail = "reader" + System.currentTimeMillis() + "@p.com";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new com.zest.productapi.dto.RegisterRequest("Read User", userEmail, "Password@123")
                        )))
                .andExpect(status().isCreated());

        String userToken = loginAndGetAccessToken(userEmail, "Password@123");

        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productName").value("Monitor"));
    }

    @Test
    void adminCreate_shouldValidateRequestBody() throws Exception {
        String adminToken = loginAndGetAccessToken("admin@test.local", "Admin@123");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"\",\"createdBy\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void listProducts_shouldRejectInvalidSortField() throws Exception {
        String adminToken = loginAndGetAccessToken("admin@test.local", "Admin@123");

        mockMvc.perform(get("/api/v1/products?sortBy=invalidField")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data")
                .path("accessToken")
                .asText();
    }
}

