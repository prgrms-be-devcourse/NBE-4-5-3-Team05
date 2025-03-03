package com.NBE_4_5_2.Team5.global.init;

import com.NBE_4_5_2.Team5.domain.product.entity.Product;
import com.NBE_4_5_2.Team5.domain.product.entity.LikedPost;
import com.NBE_4_5_2.Team5.domain.product.repository.ProductRepository;
import com.NBE_4_5_2.Team5.domain.product.repository.LikedPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final ProductRepository productRepository;
    private final LikedPostRepository likedPostRepository;

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            productInit();
            likedPostInit();
        };
    }

    @Transactional
    public void productInit() {
        if (productRepository.count() > 0) {
            return;
        }

        List<Product> products = List.of(
                Product.builder()
                        .id("ppost-11111")
                        .productName("Laptop")
                        .productPrice(1200000)
                        .title("Gaming Laptop for sale")
                        .content("Brand new gaming laptop with RTX 3060.")
                        .likedCount(5)
                        .status("purchased")
                        .sellerId("user-001")
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build(),
                Product.builder()
                        .id("ppost-22222")
                        .productName("Smartphone")
                        .productPrice(800000)
                        .title("Used iPhone 12 Pro")
                        .content("Lightly used iPhone 12 Pro, 256GB storage.")
                        .likedCount(10)
                        .status("purchased")
                        .sellerId("user-002")
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build(),
                Product.builder()
                        .id("ppost-33333")
                        .productName("Monitor")
                        .productPrice(300000)
                        .title("27-inch 4K Monitor")
                        .content("Perfect condition 27-inch 4K monitor.")
                        .likedCount(3)
                        .status("sold")
                        .sellerId("user-001")
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );

        productRepository.saveAll(products);
        System.out.println("✅ 초기 상품 데이터 (구매 내역 & 판매 내역) 삽입 완료!");
    }

    @Transactional
    public void likedPostInit() {
        if (likedPostRepository.count() > 0) {
            return;
        }

        List<LikedPost> likedPosts = List.of(
                LikedPost.builder()
                        .userId("user-001")
                        .productPostId("ppost-11111") // user-001이 Laptop 찜
                        .build(),
                LikedPost.builder()
                        .userId("user-001")
                        .productPostId("ppost-22222") // user-001이 Smartphone 찜
                        .build(),
                LikedPost.builder()
                        .userId("user-002")
                        .productPostId("ppost-33333") // user-002가 Monitor 찜
                        .build()
        );

        likedPostRepository.saveAll(likedPosts);
        System.out.println("✅ 초기 찜 데이터 삽입 완료!");
    }
}
