package com.smg.productservice.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smg.productservice.api.dto.CreateProductRequest;
import com.smg.productservice.api.dto.ProductResponse;
import com.smg.productservice.api.mapper.ProductMapper;
import com.smg.productservice.event.ProductCreatedEvent;
import java.time.Instant;

import com.smg.productservice.outbox.OutboxEvent;
import com.smg.productservice.outbox.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    public static final String PRODUCT_CREATED_TOPIC = "product.created";
    private final ProductRepository productRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ProductService(
            ProductRepository productRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.productRepository = productRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.debug("Creating product with name={}", request.name());

        Product product = new Product(request.name(), request.price());
        Product savedProduct = productRepository.save(product);

        log.info("Product created with id={}", savedProduct.getId());

        ProductCreatedEvent event = new ProductCreatedEvent(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getPrice(),
                Instant.now().toString()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                    PRODUCT_CREATED_TOPIC,
                    savedProduct.getId().toString(),
                    payload
            );

            outboxEventRepository.save(outboxEvent);

            log.info("Outbox event created for productId={}", savedProduct.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize product created event for productId={}", savedProduct.getId(), e);
            throw new IllegalStateException("Failed to serialize product created event", e);
        }

        return ProductMapper.toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        return productRepository.findById(id)
                .map(ProductMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductMapper::toResponse);
    }
}