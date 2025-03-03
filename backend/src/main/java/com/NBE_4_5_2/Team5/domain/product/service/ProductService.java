package com.NBE_4_5_2.Team5.domain.product.service;

import com.NBE_4_5_2.Team5.domain.product.dto.ProductDto;
import com.NBE_4_5_2.Team5.domain.product.entity.Product;
import com.NBE_4_5_2.Team5.domain.product.repository.LikedPostRepository;
import com.NBE_4_5_2.Team5.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final LikedPostRepository likedPostRepository;

    // 구매 내역 조회
    public List<ProductDto> getProduct() {
        List<Product> products = productRepository.findAllByStatus("purchased");
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 판매 내역 조회 (판매 완료, 판매 중, 예약됨 모두 포함)
    public List<ProductDto> getSalesHistory(String userId) {
        List<Product> products = productRepository.findAllBySellerId(userId);
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 찜한 게시글 조회
    public List<ProductDto> getFavoriteProducts(String userId) {
        List<String> likedProductIds = likedPostRepository.findAllProductPostIdsByUserId(userId);
        if (likedProductIds.isEmpty()) {
            return List.of(); // 빈 리스트 반환
        }
        List<Product> favoriteProducts = productRepository.findAllById(likedProductIds);
        return favoriteProducts.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }
}

