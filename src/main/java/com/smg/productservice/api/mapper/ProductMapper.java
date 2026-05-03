package com.smg.productservice.api.mapper;

import com.smg.productservice.api.dto.ProductResponse;
import com.smg.productservice.domain.Product;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ProductMapper {

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCreatedAt()
        );
    }
}