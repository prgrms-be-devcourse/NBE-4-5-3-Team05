package com.NBE_4_5_2.Team5.domain.post.post.controller;

import com.NBE_4_5_2.Team5.domain.post.post.entity.LikedPost;
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost;
import com.NBE_4_5_2.Team5.domain.post.post.repository.LikedPostRepository;
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository;
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService;
import com.NBE_4_5_2.Team5.domain.user.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService;
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig;
import com.NBE_4_5_2.Team5.global.init.BaseInitData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProductPostController 관련 기능(판매내역, 찜내역, 구매내역)을 통합 테스트하는 예시 코드
 */
@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductPostControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductPostService productPostService;

    @Autowired
    private ProductPostRepository productPostRepository;

    @Autowired
    private LikedPostRepository likedPostRepository; // 찜 기능 테스트 시 필요

    @Autowired
    private BaseInitData baseInitData;

    private User seller;
    private User buyer;
    private String sellerToken;
    private String buyerToken;


    @BeforeEach
    void beforeEach() {
        // 테스트용 유저 2명 사용
        seller = userService.getUserByUsername("user1").orElseThrow(
                () -> new RuntimeException("User not found")
        );
        buyer = userService.getUserByUsername("user2").orElseThrow(() -> new RuntimeException("User not found"));

        // JWT 토큰 발급
        sellerToken = userService.generateAuthTokenAsString(seller);
        buyerToken = userService.generateAuthTokenAsString(buyer);
    }

    @Test
    @DisplayName("상품 게시글 작성")
    void write1() throws Exception {
        String json = """
                    {
                      "productName": "아이폰이요 최신꺼1233",
                      "productPrice": 1200000,
                      "title": "거의 새 것 팝니다",
                      "content": "사용한 지 3개월 됨, 상태 최상",
                      "imageUrlList": [
                        "https://example.com/image1.jpg",
                        "https://example.com/image2.jpg",
                        "https://example.com/image3.jpg"
                      ],
                      "latitude": 37.5665,
                      "longitude": 126.9780,
                      "categoryIds": [1, 3, 5]
                    }
                """;

        mvc.perform(
                        post("/api/posts")
                                .header("Authorization", "Bearer " + sellerToken)
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("글 작성 성공"))
                .andExpect(jsonPath("$.data.title").value("거의 새 것 팝니다"));
    }

    @Test
    @DisplayName("전체 글 목록 조회")
    void list1() throws Exception {
        createPost(seller, "전체글");

        mvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items[0].title").value("전체글"));
    }

    @Test
    @DisplayName("글 상세 조회")
    void getPost1() throws Exception {
        ProductPost post = createPost(seller, "상세조회 상품");

        mvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("게시물 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(post.getId()));
    }

    @Test
    @DisplayName("내 글 목록 조회")
    void myPosts1() throws Exception {
        createPost(seller, "내글1");

        mvc.perform(get("/api/posts/my")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("내 글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items.size()").value(10));
    }

    @Test
    @DisplayName("최근 조회한 게시글 조회")
    void getRecentlyViewedPosts() throws Exception {
        ProductPost post = createPost(seller, "최근 본 글");

        // 상세 조회 → 최근 본 글 기록됨
        mvc.perform(
                get("/api/posts/" + post.getId())
                        .header("Authorization", "Bearer " + buyerToken)
        );

        mvc.perform(
                        get("/api/posts/recently-viewed")
                                .header("Authorization", "Bearer " + buyerToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id").value(hasItem(post.getId())));
    }


    @Test
    @DisplayName("게시글 수정")
    void modify1() throws Exception {
        ProductPost post = createPost(seller, "수정 전 제목");

        String json = """
                {
                  "productName": "수정된 상품",
                  "productPrice": 20000,
                  "title": "수정 제목",
                  "content": "수정된 내용",
                  "imageUrlList": ["http://img.com/edited.jpg"],
                  "latitude": 37.6,
                  "longitude": 127.1,
                  "categoryIds": [1]
                }
                """;

        mvc.perform(
                        put("/api/posts/" + post.getId())
                                .header("Authorization", "Bearer " + sellerToken)
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("글 수정 완료."));
    }

    @Test
    @DisplayName("게시글 삭제")
    void delete() throws Exception {
        ProductPost post = createPost(seller, "삭제 대상");

        mvc.perform(
                        MockMvcRequestBuilders.delete("/api/posts/" + post.getId())
                                .header("Authorization", "Bearer " + sellerToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("글 삭제 완료."));
    }

    @Test
    @DisplayName("게시글 찜")
    void likePost() throws Exception {
        ProductPost post = createPost(seller, "찜 대상");

        mvc.perform(
                        post("/api/posts/" + post.getId() + "/like")
                                .header("Authorization", "Bearer " + buyerToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("찜 완료"));
    }


    @Test
    @DisplayName("내 판매 내역 조회 - 배열 어디엔가 내 글이 있는지 확인")
    void testGetMySales() throws Exception {
        // GIVEN: seller가 작성한 게시글 생성
        ProductPost post = createPost(seller, "테스트 판매 상품");
        String newPostId = post.getId(); // 예: "ppost-xxxxx..."

        // WHEN: seller로 /api/posts/my/sales 호출
        ResultActions resultActions = mvc.perform(
                get("/api/posts/my/sales")
                        .header("Authorization", "Bearer " + sellerToken)
        ).andDo(print());

        // THEN: 순서(배열 0번 등)가 아니라 "어디든 해당 ID가 포함"되었는지 확인
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("내 판매 내역 조회 성공"))
                // → data 배열 안의 모든 요소(id 필드) 중에 newPostId가 있으면 OK
                .andExpect(jsonPath("$.data[*].id").value(hasItem(newPostId)));
    }

    @Test
    @DisplayName("내가 찜한 내역 조회")
    void testGetMyFavorites() throws Exception {
        // GIVEN: seller가 작성한 게시글
        ProductPost post = createPost(seller, "찜 테스트 상품");

        // buyer가 이 게시글을 찜한 상태로 만들기
        LikedPost likedPost = new LikedPost(
                buyer.getId(),
                post.getId()
        );
        likedPostRepository.save(likedPost);

        // WHEN: buyer 토큰으로 /api/posts/my/favorites 호출
        ResultActions resultActions = mvc.perform(
                get("/api/posts/my/favorites")
                        .header("Authorization", "Bearer " + buyerToken)
        ).andDo(print());

        // THEN
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("내가 찜한 내역 조회 성공"))
                .andExpect(jsonPath("$.data.items[0].id").value(post.getId()))
                .andExpect(jsonPath("$.data.items[0].writerId").value(seller.getId()));
    }

    @Test
    @DisplayName("내가 구매한 내역 조회")
    void testGetMyPurchases() throws Exception {
        // GIVEN: seller가 작성한 게시글
        ProductPost post = createPost(seller, "구매 테스트 상품");

        // buyer가 이 글을 구매한 것으로 처리
        productPostService.purchasePost(buyer, post.getId());

        // WHEN: buyer 토큰으로 /api/posts/my/purchases 호출
        ResultActions resultActions = mvc.perform(
                get("/api/posts/my/purchases")
                        .header("Authorization", "Bearer " + buyerToken)
        ).andDo(print());

        // THEN
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("내 구매 내역 조회 성공"))
                .andExpect(jsonPath("data.items[0].id").value(post.getId()))
                .andExpect(jsonPath("$.data.items[0].writerId").value(seller.getId()));
    }

    // ---------------------------
    // 테스트용 게시글 생성 메서드
    private ProductPost createPost(User writer, String title) {
        ProductPost post = new ProductPost(
                writer,
                "상품명",
                10000,
                title,
                "테스트 내용",
                "https://example.com/1.jpg",
                37.5f,
                127.0f
        );
        return productPostRepository.save(post);
    }
}
