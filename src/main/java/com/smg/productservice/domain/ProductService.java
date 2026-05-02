package com.smg.productservice.domain;

import com.smg.productservice.api.dto.CreateProductRequest;
import com.smg.productservice.api.dto.ProductResponse;
import com.smg.productservice.api.mapper.ProductMapper;
import com.smg.productservice.event.ProductCreatedEvent;
import com.smg.productservice.event.ProductEventPublisher;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventPublisher productEventPublisher;

    public ProductService(ProductRepository productRepository, ProductEventPublisher productEventPublisher) {
        this.productRepository = productRepository;
        this.productEventPublisher = productEventPublisher;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product(request.name(), request.price());

        Product savedProduct = productRepository.save(product);

        ProductCreatedEvent event = new ProductCreatedEvent(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getPrice(),
                Instant.now().toString()
        );

        productEventPublisher.publishProductCreated(event);

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