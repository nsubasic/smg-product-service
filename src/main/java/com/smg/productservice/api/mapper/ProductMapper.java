package com.smg.productservice.api.mapper;

import com.smg.productservice.api.dto.ProductResponse;
import com.smg.productservice.domain.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCreatedAt()
        );
    }
}