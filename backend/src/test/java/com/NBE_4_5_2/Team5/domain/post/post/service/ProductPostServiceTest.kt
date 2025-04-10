package com.NBE_4_5_2.Team5.domain.post.post.service

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category
import com.NBE_4_5_2.Team5.domain.post.category.repository.CategoryRepository
import com.NBE_4_5_2.Team5.domain.post.post.dto.request.ProductPostWriteForm
import com.NBE_4_5_2.Team5.domain.user.user.entity.User
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.global.config.BaseTestConfig
import com.NBE_4_5_2.Team5.global.exception.ServiceException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@BaseTestConfig
class ProductPostServiceTest {
    @Autowired
    lateinit var productPostService: ProductPostService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var categoryRepository: CategoryRepository


    lateinit var testUser: User
    lateinit var categoryIds: List<Long>

    @BeforeEach
    fun setUp() {
        testUser = userService.getUserByUsername("user1").orElseThrow {
            ServiceException(
                "404-1",
                "존재하지 않습니다"
            )
        }
        categoryIds = categoryRepository.findAll().stream()
            .map(Category::id)
            .limit(1)
            .toList()
    }

    @Test
    fun write() {
        val form = ProductPostWriteForm(
            "테스트 상품",
            12345,
            "테스트 제목",
            "테스트 내용입니다.",
            categoryIds,
            listOf("http://localhost/images/test.jpg"),
            37.5f,
            127.0f,
            "거래위치"
        )

        val response = productPostService.write(testUser, form)

        Assertions.assertThat(response.title).isEqualTo("테스트 제목")
    }

    @Test
    fun getPosts() {
        val posts = productPostService.getPosts(1, 10, "", "desc",0,10000000, emptyList())
        val items = posts.items

        assertThat(items.size).isEqualTo(10)
    }

    @Test
    fun getPost() {
        val res = productPostService.getPosts(1, 1, "", "desc",0,10000000, emptyList()).items[0]
        val post = productPostService.getPost(res.id)

        assertThat(res.id).isEqualTo(post.id)
    }
}
