package com.NBE_4_5_2.Team5.global.init;

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category;
import com.NBE_4_5_2.Team5.domain.post.category.repository.CategoryRepository;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductCategory;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductCategoryRepository;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.product.entity.Product;
import com.NBE_4_5_2.Team5.domain.product.repository.ProductRepository;
import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.repository.UserRepository;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final CategoryRepository categoryRepository;
    private final ProductPostRepository postRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    @Lazy
    private BaseInitData self;

    @Bean
    @Order(1)
    public ApplicationRunner applicationRunner1() {
        return args -> {
            self.userInit();
        };
    }

    @Bean
    @Order(2)
    public ApplicationRunner applicationRunner2() {
        return args -> {
            self.categoryInit();
        };
    }

    @Bean
    @Order(3)
    public ApplicationRunner applicationRunner3() {
        return args -> {
            self.postInit();
        };
    }

    @Transactional
    public void userInit() {

        if (userService.count() > 0) {
            return;
        }

        userService.signup("user1", "user11234@", "user1@gmail.com", "user1", "서울시 강남구", "https://example.com/default_profile.png");
        userService.signup("user2", "user21234@", "user2@gmail.com", "user2", "서울시 강서구", "https://example.com/default_profile.png");
        userService.signup("user3", "user31234@", "user3@gmail.com", "user3", "서울시 광진구", "https://example.com/default_profile.png");

    }

    @Transactional
    public void categoryInit() {
        if (categoryRepository.count() > 0) {
            return; // 이미 카테고리가 존재하면 초기화하지 않음
        }

        List<Category> categories = List.of(
                new Category(null, "전자제품"),
                new Category(null, "가구"),
                new Category(null, "의류"),
                new Category(null, "스포츠 용품"),
                new Category(null, "도서"),
                new Category(null, "생활용품"),
                new Category(null, "자동차 용품"),
                new Category(null, "식품"),
                new Category(null, "악기"),
                new Category(null, "반려동물 용품"),
                new Category(null, "뷰티/미용"),
                new Category(null, "티켓/쿠폰"),
                new Category(null, "수집/예술"),
                new Category(null, "게임"),
                new Category(null, "기타")
        );

        categoryRepository.saveAll(categories);
    }

    @Transactional
    public void postInit() {
        if (postRepository.count() > 0) {
            return;
        }

        List<User> users = userRepository.findAll();
        System.out.println(users.size());
        List<Category> categories = categoryRepository.findAll();

        List<ProductPost> posts = new ArrayList<>();

        // 0,1,2
        for (int i = 1; i <= 50; i++) {
            User writer = users.get((i - 1) % users.size());
            posts.add(ProductPost.create(
                    writer,
                    "상품 " + i,
                    (i * 10000) % 200000 + 10000, // 가격 랜덤화
                    "제목 " + i,
                    "이것은 테스트 상품 " + i + " 입니다.",
                    "https://example.com/product" + i + "_1.jpg,https://example.com/product" + i + "_2.jpg",
                    37.5f + (i % 10) * 0.01f, // 위치 랜덤화
                    127.0f + (i % 10) * 0.01f
            ));
        }

        postRepository.saveAll(posts);

        // ✅ `ProductCategory` 생성하여 게시글과 랜덤 카테고리 연결
        List<ProductCategory> productCategories = new ArrayList<>();

        for (int i = 0; i < posts.size(); i++) {
            Category randomCategory = categories.get(i % categories.size()); // ✅ 순환하면서 랜덤 카테고리 적용
            productCategories.add(ProductCategory.builder()
                    .productPost(posts.get(i))
                    .category(randomCategory)
                    .build());
        }

        productCategoryRepository.saveAll(productCategories);
    }

    private final ProductRepository productRepository;

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> productInit();
    }

    @Transactional
    public void productInit() {
        if (productRepository.count() > 0) {
            return; // 기존 데이터가 있으면 초기화하지 않음
        }

        List<Product> products = List.of(
                // ✅ 구매 내역 데이터
                Product.builder()
                        .id("ppost-11111")
                        .productName("Laptop")
                        .productPrice(1200000)
                        .title("Gaming Laptop for sale")
                        .content("Brand new gaming laptop with RTX 3060.")
                        .likedCount(5)
                        .status("purchased") // 구매 완료
                        .sellerId("user-001") // 판매자 ID 추가
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

                // ✅ 판매 내역 데이터 (판매 완료)
                Product.builder()
                        .id("ppost-33333")
                        .productName("Monitor")
                        .productPrice(300000)
                        .title("27-inch 4K Monitor")
                        .content("Perfect condition 27-inch 4K monitor.")
                        .likedCount(3)
                        .status("sold") // 판매 완료
                        .sellerId("user-001") // 판매자 ID 추가
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build(),

                // ✅ 판매 진행 중 데이터
                Product.builder()
                        .id("ppost-44444")
                        .productName("Mechanical Keyboard")
                        .productPrice(150000)
                        .title("Cherry MX Mechanical Keyboard")
                        .content("Barely used Cherry MX keyboard, great for gaming.")
                        .likedCount(7)
                        .status("selling") // 판매 진행 중
                        .sellerId("user-002")
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build(),

                // ✅ 예약된 상품 데이터
                Product.builder()
                        .id("ppost-55555")
                        .productName("Gaming Chair")
                        .productPrice(250000)
                        .title("Ergonomic Gaming Chair")
                        .content("High-quality ergonomic gaming chair, almost new.")
                        .likedCount(9)
                        .status("reserved") // 예약됨
                        .sellerId("user-003")
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build()
        );

        productRepository.saveAll(products);
        System.out.println("✅ 초기 상품 데이터 (구매 내역 & 판매 내역) 삽입 완료!");
    }
}

