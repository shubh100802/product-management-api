package com.zest.productapi.service.impl;

// ==========file-context==========

import com.zest.productapi.dto.ItemResponse;
import com.zest.productapi.dto.PageResponse;
import com.zest.productapi.dto.ProductCreateRequest;
import com.zest.productapi.dto.ProductResponse;
import com.zest.productapi.dto.ProductUpdateRequest;
import com.zest.productapi.entity.Product;
import com.zest.productapi.exception.ResourceNotFoundException;
import com.zest.productapi.mapper.ProductMapper;
import com.zest.productapi.repository.ItemRepository;
import com.zest.productapi.repository.ProductRepository;
import com.zest.productapi.service.AuditLogService;
import com.zest.productapi.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;
    private final ProductMapper productMapper;
    private final AuditLogService auditLogService;

    public ProductServiceImpl(ProductRepository productRepository,
                              ItemRepository itemRepository,
                              ProductMapper productMapper,
                              AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
        this.productMapper = productMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(String name, Pageable pageable) {
        // ==========filtered-or-full-list==========
        Page<Product> products = (name == null || name.isBlank())
                ? productRepository.findAll(pageable)
                : productRepository.findByProductNameContainingIgnoreCase(name, pageable);

        List<ProductResponse> content = products.stream()
                .map(productMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        // ==========create-product==========
        Product product = new Product();
        product.setProductName(request.productName());
        product.setCreatedBy(request.createdBy());

        Product saved = productRepository.save(product);
        auditLogService.logProductEvent("CREATE", saved.getId(), request.createdBy());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        // ==========update-product==========
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productMapper.applyUpdates(product, request);
        Product saved = productRepository.save(product);
        auditLogService.logProductEvent("UPDATE", saved.getId(), request.modifiedBy());
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        // ==========delete-product==========
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        auditLogService.logProductEvent("DELETE", id, "system");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return itemRepository.findByProductId(productId)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }
}
