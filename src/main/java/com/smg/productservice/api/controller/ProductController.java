package com.smg.productservice.api.controller;

import com.smg.productservice.api.dto.CreateProductRequest;
import com.smg.productservice.api.dto.ProductResponse;
import com.smg.productservice.domain.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = productService.createProduct(request);

        return ResponseEntity
                .created(URI.create("/products/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getProducts(pageable));
    }
}