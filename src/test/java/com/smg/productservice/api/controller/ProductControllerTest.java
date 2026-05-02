package com.smg.productservice.api.controller;

import com.smg.productservice.api.dto.CreateProductRequest;
import com.smg.productservice.api.dto.ProductResponse;
import com.smg.productservice.domain.ProductNotFoundException;
import com.smg.productservice.domain.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private static final String PRODUCT_NAME_VALID = "Product 1";
    private static final BigDecimal PRODUCT_PRICE_VALID = BigDecimal.valueOf(12345.67);


    @Test
    void createProduct_returnsCreatedProduct() throws Exception {
        UUID id = UUID.randomUUID();

        CreateProductRequest request =
                new CreateProductRequest(PRODUCT_NAME_VALID, PRODUCT_PRICE_VALID);

        ProductResponse response =
                new ProductResponse(id, PRODUCT_NAME_VALID, PRODUCT_PRICE_VALID, Instant.now());

        when(productService.createProduct(request)).thenReturn(response);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "price": %s
                                }
                                """.formatted(PRODUCT_NAME_VALID, PRODUCT_PRICE_VALID)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/products/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value(PRODUCT_NAME_VALID))
                .andExpect(jsonPath("$.price").value(PRODUCT_PRICE_VALID));
    }

    @Test
    void createProduct_whenRequestInvalid_returnsBadRequest() throws Exception {
        String invalidRequest = """
                {
                  "name": "",
                  "price": -1
                }
                """;

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void getProductById_whenProductExists_returnsProduct() throws Exception {
        UUID id = UUID.randomUUID();

        ProductResponse response =
                new ProductResponse(id, PRODUCT_NAME_VALID, PRODUCT_PRICE_VALID, Instant.now());

        when(productService.getProductById(id)).thenReturn(response);

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value(PRODUCT_NAME_VALID))
                .andExpect(jsonPath("$.price").value(PRODUCT_PRICE_VALID));
    }

    @Test
    void getProductById_whenProductMissing_returnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(productService.getProductById(id))
                .thenThrow(new ProductNotFoundException(id));

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"));
    }
}