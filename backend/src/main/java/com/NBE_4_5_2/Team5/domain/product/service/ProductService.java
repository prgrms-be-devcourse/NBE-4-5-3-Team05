package com.NBE_4_5_2.Team5.domain.product.service;

import com.NBE_4_5_2.Team5.domain.product.dto.ProductDto;
import com.NBE_4_5_2.Team5.domain.product.entity.Product;
import com.NBE_4_5_2.Team5.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;


    //구매 내역 조회
    public List<ProductDto> getProduct() {
        List<Product> products = productRepository.findAllByStatus("purchased");
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    //판매 내역 조회
    public List<ProductDto> getSalesHistory(String userId, String status) {
        List<Product> products = productRepository.findAllBySellerIdAndStatus(userId, status);
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }
}
