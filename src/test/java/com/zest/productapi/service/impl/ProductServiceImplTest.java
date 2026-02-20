package com.zest.productapi.service.impl;

// ==========file-context==========

import com.zest.productapi.dto.ProductCreateRequest;
import com.zest.productapi.dto.ProductUpdateRequest;
import com.zest.productapi.entity.Product;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.mapper.ProductMapper;
import com.zest.productapi.repository.ItemRepository;
import com.zest.productapi.repository.ProductRepository;
import com.zest.productapi.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private AuditLogService auditLogService;

    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
        productService = new ProductServiceImpl(productRepository, itemRepository, productMapper, auditLogService);
    }

    @Test
    void getProducts_shouldReturnPagedData() {
        Product product = new Product();
        product.setId(1L);
        product.setProductName("Keyboard");
        product.setCreatedBy("admin");

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(page);

        var response = productService.getProducts(null, PageRequest.of(0, 10));

        assertEquals(1, response.content().size());
        assertEquals("Keyboard", response.content().get(0).productName());
        verify(productRepository).findAll(any(PageRequest.class));
    }

    @Test
    void createProduct_shouldPersistAndReturnResponse() {
        Product saved = new Product();
        saved.setId(10L);
        saved.setProductName("Mouse");
        saved.setCreatedBy("admin");

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        var response = productService.createProduct(new ProductCreateRequest("Mouse", "admin"));

        assertEquals(10L, response.id());
        assertEquals("Mouse", response.productName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(99L, new ProductUpdateRequest("Phone", "admin")));
    }

    @Test
    void deleteProduct_shouldCallRepositoryDelete() {
        when(productRepository.existsById(5L)).thenReturn(true);

        productService.deleteProduct(5L);

        verify(productRepository).deleteById(5L);
    }
}

