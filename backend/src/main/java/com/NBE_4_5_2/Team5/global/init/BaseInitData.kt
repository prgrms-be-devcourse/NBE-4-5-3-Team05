package com.NBE_4_5_2.Team5.global.init

import com.NBE_4_5_2.Team5.domain.post.category.entity.Category
import com.NBE_4_5_2.Team5.domain.post.category.repository.CategoryRepository
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductCategory
import com.NBE_4_5_2.Team5.domain.post.post.entity.ProductPost
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductCategoryRepository
import com.NBE_4_5_2.Team5.domain.post.post.repository.ProductPostRepository
import com.NBE_4_5_2.Team5.domain.user.admin.entity.NoticePost
import com.NBE_4_5_2.Team5.domain.user.admin.repository.NoticePostRepository
import com.NBE_4_5_2.Team5.domain.user.admin.service.AdminService
import com.NBE_4_5_2.Team5.domain.user.user.entity.Role
import com.NBE_4_5_2.Team5.domain.user.user.repository.UserRepository
import com.NBE_4_5_2.Team5.domain.user.user.service.UserService
import com.NBE_4_5_2.Team5.domain.user.user.service.email.EmailService
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order


@Configuration
@Profile("!monitor")
class BaseInitData(
    private val categoryRepository: CategoryRepository,
    private val postRepository: ProductPostRepository,
    private val productCategoryRepository: ProductCategoryRepository,
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val adminService: AdminService,
    private val noticePostRepository: NoticePostRepository,
    private val emailService: EmailService,
    @Value("\${custom.server.host}")
    private val serverHost: String
) {
    @Autowired
    @Lazy
    private lateinit var self: BaseInitData

    @Bean
    @Order(1)
    fun applicationRunner1(): ApplicationRunner =
        ApplicationRunner { _ -> self.userInit() }

    @Bean
    @Order(2)
    fun applicationRunner2(): ApplicationRunner =
        ApplicationRunner { _ -> self.categoryInit() }

    @Bean
    @Order(3)
    fun applicationRunner3(): ApplicationRunner =
        ApplicationRunner { _ -> self.postInit() }

    @Bean
    @Order(4)
    fun applicationRunner4(): ApplicationRunner =
        ApplicationRunner { _ -> self.noticeInit() }

    @Transactional
    fun userInit() {
        if (userRepository.count() > 0) return

        emailService.saveVerificationCode("user1@gmail.com", "verified")
        emailService.saveVerificationCode("user2@gmail.com", "verified")
        emailService.saveVerificationCode("user3@gmail.com", "verified")
        emailService.saveVerificationCode("user4@gmail.com", "verified")
        emailService.saveVerificationCode("admin2@gmail.com", "verified")

        val baseUrl =
            if (serverHost.startsWith("backend.nbe-4-5-5-team5.shop"))
                "https://%s/images".formatted(serverHost)
            else
                "http://localhost:8080/images/"
        val imageUrl = baseUrl + "default_profile" + ".jpg" // 하나의 이미지만 사용

        userService.createUser("user1", "user11234@", "user1@gmail.com", "user1", "서울시 강남구", imageUrl)
        userService.createUser("user2", "user21234@", "user2@gmail.com", "user2", "서울시 강서구", imageUrl)
        userService.createUser("user3", "user31234@", "user3@gmail.com", "user3", "서울시 광진구", imageUrl)

        adminService.signUpAdmin("admin2", "password2", "admin2", "admin2@gmail.com")
        adminService.signUpAdmin("user4", "user41234@", "admin4", "user4@gmail.com")
    }

    @Transactional
    fun categoryInit() {
        if (categoryRepository.count() > 0) return  // 이미 카테고리 존재 시 초기화 생략

        val categories = listOf(
            Category("전자제품"),
            Category("가구"),
            Category("의류"),
            Category("스포츠 용품"),
            Category("도서"),
            Category("생활용품"),
            Category("자동차 용품"),
            Category("식품"),
            Category("악기"),
            Category("반려동물 용품"),
            Category("뷰티/미용"),
            Category("티켓/쿠폰"),
            Category("수집/예술"),
            Category("게임"),
            Category("기타")
        )
        categoryRepository.saveAll(categories)
    }

    @Transactional
    fun postInit() {
        if (postRepository.count() > 0) return

        val users = userRepository.findAll()
        val categories = categoryRepository.findAll()
        val posts = mutableListOf<ProductPost>()
        val baseUrl =
            if (serverHost.startsWith("backend.nbe-4-5-5-team5.shop"))
                "https://%s/images".formatted(serverHost)
            else
                "http://localhost:8080/images/"

        for (i in 1..50) {
            val writer = users[(i - 1) % users.size]
            val imageUrl = baseUrl + "default" + ".jpg"
            posts.add(
                ProductPost.create(
                    writer,
                    "상품 $i",
                    (i * 10000) % 200000 + 10000,
                    "제목 $i",
                    "이것은 테스트 상품 $i 입니다.",
                    imageUrl,
                    37.5f + (i % 10) * 0.01f,
                    127.0f + (i % 10) * 0.01f
                )
            )
        }
        postRepository.saveAll(posts)

        val productCategories = mutableListOf<ProductCategory>()
        for (i in posts.indices) {
            val randomCategory = categories[i % categories.size]
            productCategories.add(ProductCategory(posts[i], randomCategory))
        }
        productCategoryRepository.saveAll(productCategories)
    }

    @Transactional
    fun noticeInit() {
        if (noticePostRepository.count() > 0) return

        var admin = userRepository.findAll().firstOrNull { it.role == Role.ADMIN }
        if (admin == null && userRepository.findAll().isNotEmpty()) {
            admin = userRepository.findAll()[0]
        }
        for (i in 1..10) {
            val notice = NoticePost(
                "공지사항 제목 $i",
                "공지사항 내용 $i - 중요한 공지사항 내용입니다.",
                admin
            )
            noticePostRepository.save(notice)
        }
    }
}
