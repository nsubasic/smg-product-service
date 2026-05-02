package com.smg.productservice.domain;

import com.smg.productservice.api.dto.CreateProductRequest;
import com.smg.productservice.api.dto.ProductResponse;
import com.smg.productservice.event.ProductEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ProductEventPublisher productEventPublisher = mock(ProductEventPublisher.class);

    private final ProductService productService =
            new ProductService(productRepository, productEventPublisher);

    private static final String PRODUCT_NAME_VALID = "Product 1";
    private static final BigDecimal PRODUCT_PRICE_VALID = BigDecimal.valueOf(12345.67);

    @Test
    void createProduct_savesProductAndPublishesEvent() {
        CreateProductRequest request =
                new CreateProductRequest(PRODUCT_NAME_VALID, PRODUCT_PRICE_VALID);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(request);

        assertThat(response.name()).isEqualTo(PRODUCT_NAME_VALID);
        assertThat(response.price()).isEqualByComparingTo(PRODUCT_PRICE_VALID);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        assertThat(productCaptor.getValue().getName()).isEqualTo(PRODUCT_NAME_VALID);
        assertThat(productCaptor.getValue().getPrice()).isEqualByComparingTo(PRODUCT_PRICE_VALID);

        verify(productEventPublisher).publishProductCreated(any());
    }

    @Test
    void getProductById_whenProductDoesNotExist_throwsException() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void getProducts_returnsPagedProducts() {
        PageRequest pageRequest = PageRequest.of(0, 20);

        when(productRepository.findAll(pageRequest))
                .thenReturn(Page.empty(pageRequest));

        productService.getProducts(pageRequest);

        verify(productRepository).findAll(pageRequest);
    }
}