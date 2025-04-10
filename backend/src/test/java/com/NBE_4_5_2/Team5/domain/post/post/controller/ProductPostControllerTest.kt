package com.NBE_4_5_2.Team5.domain.post.post.controller;

import com.NBE_4_5_2.Team5.domain.post.post.entity.LikedPost
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.post.post.repository.LikedPostRepository
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository
import com.NBE_4_5_2.Team5.domain.post.post.service.ProductPostService
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.NBE_4_5_2.Team5.global.init.BaseInitData
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc

import org.hamcrest.Matchers.hasItem
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * ProductPostController 관련 기능(판매내역, 찜내역, 구매내역)을 통합 테스트하는 예시 코드
 */
@SpringBootTest
@AutoConfigureMockMvc
@BaseTestConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductPostControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var productPostService: ProductPostService

    @Autowired
    lateinit var productPostRepository: ProductPostRepository

    @Autowired
    lateinit var likedPostRepository: LikedPostRepository


    private lateinit var seller: User
    private lateinit var buyer: User
    private lateinit var sellerToken: String
    private lateinit var buyerToken: String

    @BeforeEach
    fun beforeEach() {
        seller = userService.getUserByUsername("user1").orElseThrow()
        buyer = userService.getUserByUsername("user2").orElseThrow()
        sellerToken = userService.generateAuthTokenAsString(seller)
        buyerToken = userService.generateAuthTokenAsString(buyer)
    }

    @Test
    fun `상품 게시글 작성`() {
        val json = """
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
              "categoryIds": [1, 3, 5],
              "location": "거래위치"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/posts")
                .header("Authorization", "Bearer $sellerToken")
                .contentType("application/json")
                .content(json)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("글 작성 성공"))
            .andExpect(jsonPath("$.data.title").value("거의 새 것 팝니다"))
    }

    @Test
    fun `전체 글 목록 조회`() {
        createPost(seller, "전체글")

        mvc.perform(get("/api/posts"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("글 목록 조회가 완료되었습니다."))
            .andExpect(jsonPath("$.data.items[0].title").value("전체글"))
    }

    @Test
    fun `글 상세 조회`() {
        val post = createPost(seller, "상세조회 상품")

        mvc.perform(get("/api/posts/${post.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("게시물 조회가 완료되었습니다."))
            .andExpect(jsonPath("$.data.id").value(post.id))
    }

    @Test
    fun `내 글 목록 조회`() {
        createPost(seller, "내글1")

        mvc.perform(
            get("/api/posts/my")
                .header("Authorization", "Bearer $sellerToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("내 글 목록 조회가 완료되었습니다."))
            .andExpect(jsonPath("$.data.items.size()").value(10))
    }

    @Test
    fun `최근 조회한 게시글 조회`() {
        val post = createPost(seller, "최근 본 글")

        mvc.perform(get("/api/posts/${post.id}").header("Authorization", "Bearer $buyerToken"))

        mvc.perform(get("/api/posts/recently-viewed").header("Authorization", "Bearer $buyerToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[*].id").value(hasItem(post.id)))
    }

    @Test
    fun `게시글 수정`() {
        val post = createPost(seller, "수정 전 제목")

        val json = """
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
        """.trimIndent()

        mvc.perform(
            put("/api/posts/${post.id}")
                .header("Authorization", "Bearer $sellerToken")
                .contentType("application/json")
                .content(json)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("글 수정 완료."))
    }

    @Test
    fun `게시글 삭제`() {
        val post = createPost(seller, "삭제 대상")

        mvc.perform(
            delete("/api/posts/${post.id}")
                .header("Authorization", "Bearer $sellerToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("글 삭제 완료."))
    }

    @Test
    fun `게시글 찜`() {
        val post = createPost(seller, "찜 대상")

        mvc.perform(
            post("/api/posts/${post.id}/like")
                .header("Authorization", "Bearer $buyerToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("찜 완료"))
    }

    @Test
    fun `내 판매 내역 조회`() {
        val post = createPost(seller, "테스트 판매 상품")
        val newPostId = post.id

        mvc.perform(get("/api/posts/my/sales").header("Authorization", "Bearer $sellerToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("내 판매 내역 조회 성공"))
            .andExpect(jsonPath("$.data[*].id").value(hasItem(newPostId)))
    }

    @Test
    fun `내가 찜한 내역 조회`() {
        val post = createPost(seller, "찜 테스트 상품")
        likedPostRepository.save(LikedPost(buyer.id, post.id))

        mvc.perform(get("/api/posts/my/favorites").header("Authorization", "Bearer $buyerToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("내가 찜한 내역 조회 성공"))
            .andExpect(jsonPath("$.data.items[0].id").value(post.id))
            .andExpect(jsonPath("$.data.items[0].writerId").value(seller.id))
    }

    @Test
    fun `내가 구매한 내역 조회`() {
        val post = createPost(seller, "구매 테스트 상품")
        productPostService.purchasePost(buyer, post.id)

        mvc.perform(get("/api/posts/my/purchases").header("Authorization", "Bearer $buyerToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.message").value("내 구매 내역 조회 성공"))
            .andExpect(jsonPath("data.items[0].id").value(post.id))
            .andExpect(jsonPath("$.data.items[0].writerId").value(seller.id))
    }

    private fun createPost(writer: User, title: String): ProductPost {
        val post = ProductPost(
            writer,
            "상품명",
            10000,
            title,
            "테스트 내용",
            "https://example.com/1.jpg",
            37.5f,
            127.0f,
            "주소 1"
        )
        return productPostRepository.save(post)
    }
}
